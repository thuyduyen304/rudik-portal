$(document).ready(
    function() {
        $('#search-form #knowledgeBase')
            .change(
                function() {
                    knowledge_base = $("#knowledgeBase").val();
                    $
                        .ajax({
                            type: "GET",
                            contentType: "application/json",
                            url: "/api/rules/" + knowledge_base + "/predicates",
                            dataType: 'json',
                            cache: false,
                            timeout: 600000,
                            success: function(data) {
                                var html = '<option value="none">--None--</option>';
                                var len = data.length;
                                for (var i = 0; i < len; i++) {
                                    html += '<option value="' +
                                        data[i] +
                                        '">' +
                                        data[i] +
                                        '</option>';
                                }
                                html += '</option>';
                                $('#predicate').html(html);

                            },
                            error: function(e) {
                                console.log("ERROR : ",
                                    e);

                            }
                        });
                });

        $("#search-form").submit(function(event) {
            event.preventDefault();
            search_rules_submit();
        });

        $("#results-table").on("submit", ".editable form", function(e) {
            e.preventDefault();
            
            var params = {}
            
            row_idx = parseInt(this.elements["rowIdx"].value);
            column_idx = parseInt(this.elements["columnIdx"].value);
            if(column_idx == 3) {
//            	params["qualityEvaluation"] = parseInt(this.elements["qualityEvaluation"].value);
            	params["rating"] = parseInt(this.elements["qualityEvaluation"].value);
            	params["field"] = "quality_evaluation";
            } else if(column_idx == 4) {
            	params["rating"] = parseFloat(this.elements["humanConfidence"].value);
            	params["field"] = "human_confidence";
            }

            table = $("#results-table").DataTable();
            $.ajax({
                type: "PUT",
                contentType: "application/json",
                url: this.attributes['action'].value,
                data: JSON.stringify(params),
                dataType: 'json',
                cache: false,
                timeout: 600000,
                success: function(modified_rule) {
                	cell_data = table.cell(row_idx, column_idx).data();
                    table.cell(row_idx, column_idx).data(cell_data).draw();
//                	if(column_idx == 3) {
////                		cell_data = table.cell(row_idx, column_idx).data(modified_rule.qualityEvaluation).draw();
//                		cell_data = table.cell(row_idx, column_idx).data();
//                        table.cell(row_idx, column_idx).data(cell_data).draw();
//                    } else if(column_idx == 4) {
//                    	cell_data = table.cell(row_idx, column_idx).data(modified_rule.humanConfidence).draw();
//                    }
                	$('#appMsg').html('<div class="alert alert-info">Your vote has been recorded.</div>');
                },
                error: function(e) {
                    cell_data = table.cell(row_idx, column_idx).data();
                    table.cell(row_idx, column_idx).data(cell_data).draw();
                    $('#appMsg').html('<div class="alert alert-error">Error. Cannot process the request.</div>');
                }
            });
        });

        $("#results-table").on("click", "form button[value='Cancel']", function() {
            table = $("#results-table").DataTable();
            var form = this.form;
            row_idx = parseInt(form.elements["rowIdx"].value);
            column_idx = parseInt(form.elements["columnIdx"].value);
            cell_data = table.cell(row_idx, column_idx).data();
            table.cell(row_idx, column_idx).data(cell_data).draw();
        });

        $("#results-table").on("click", "a.popup-opener", function() {
            rule_id = $(this).data('ruleId');
            $("#popup-rule-" + rule_id).dialog("open");
        });

        $("#results-table").on("click", "td.editable", function() {
        	$(this).addClass('active');
            var table = $("#results-table").DataTable();
            cell_edit = this;
            idx = table.cell(this).index();
            row_data = table.rows(idx.row).data();
            value = table.cell(this).data(); //row_data[0].qualityEvaluation
            form_elm = add_input_elm(idx, row_data[0].ruleId, value);
            $(this).html(form_elm);
        });

        $("#results-table").on("click", "form", function(e) {
            e.stopPropagation();
        });
        
        $("#import-form").on('submit', function() {
        	var $this = $("#import-form button[type='submit']");
//        	var $this = $(this);
            var loadingText = 'Importing...';
            $this.data('original-text', $(this).html());
            $this.html(loadingText);
            $this.attr("disabled", "disabled")
            
            if ($(this).html() !== loadingText) {
              
            }
          });

    });

function search_rules_submit() {

    var search = {}
    search["knowledgeBase"] = $("#knowledgeBase").val();
    search["predicate"] = $("#predicate").val();
    search["ruleType"] = $("#ruleType").val();
    search["humanConfidenceFrom"] = $("#humanConfidenceFrom").val();
    search["humanConfidenceTo"] = $("#humanConfidenceTo").val();

    $("#btn-search").prop("disabled", true);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/rules",
        data: JSON.stringify(search),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function(data) {
            $('#appMsg').html('');
            $('#resultsBlock').show();
            $("#btn-search").prop("disabled", false);

            if ($.fn.dataTable.isDataTable("#results-table")) {
                var table = $("#results-table").DataTable();
                table.clear();
                table.rows.add(data);
                table.draw();
            } else {
                var table = $('#results-table').DataTable({
                    //                	destroy:true,
                    fixedHeader: true,
                    data: data,
                    "columns": [{
                        data: null,
                        defaultContent: '',
                        className: 'select-checkbox',
                        orderable: false
                    }, {
                        "data": "rule_type",
                        className: "dt-center",
                        "render": function(data, type, row, meta) {
                            if (type === 'display') {
                                if (data == true) {
                                    data = '+';
                                } else {
                                    data = '-';
                                }
                            }

                            return data;
                        }
                    }, {
                        "data": "premise",
                        "render": function(data, type, row, meta) {
                            if (type === 'display') {
                                if (row.rule_type == true) {
                                    data = trim_prefix(row.premise) + ' \u21D2 ' +
                                        trim_prefix(row.conclusion);
                                } else {
                                    data = trim_prefix(row.premise) + ' \u0026	' +
                                        trim_prefix(row.conclusion) + ' \u21D2 ' +
                                        ' \u22A5 ';
                                }
                                data = '<a class="popup-opener" data-rule-id="' + row.ruleId + '" href="#">' + data + '</a>';
                            }

                            return data;
                        }
                    }, {
                        "data": "quality_evaluation",
                        className: "dt-center editable",
                        "render": function(data, type, row, meta) {
                            if (type === 'display') {
                                data = '<div data-toggle="tooltip" data-placement="top" title="Click to vote">' +
                                	 data + 
                                	'</div>';
                            }

                            return data;
                        }
                    }, {
                        "data": "human_confidence",
                        className: "dt-center editable",
                        "render": function(data, type, row, meta) {
                            if (type === 'display') {
                                data = '<div data-toggle="tooltip" data-placement="top" title="Click to edit">' +
                                	'<a href="/instance/sample?rule_id=' + row.ruleId + '" >' + data + '</a>' +
                                	'</div>';
                            }

                            return data;
                        }
                    }, {
                        "data": "computed_confidence",
                        className: "dt-center",
                        "render": function(data, type, row, meta) {
                            if (type === 'display') {
                                data = parseFloat(Math.round(data * 100) / 100).toFixed(2);;
                            }

                            return data;
                        }
                    }, {
                        "data": "ruleId",
                        className: "dt-center",
                        "render": function(data, type, row, meta) {
                            if (type === 'display') {
                                data = '<a class="btn btn-info" role="button" href="/instance/all?rule_id=' + data + '" >Get instances</a>';
                            }

                            return data;
                        }
                    }],
                    select: {
                        style: 'multi+shift',
                        selector: 'td:first-child'
                    },
                    dom: 'Bfrtip',
                    buttons: [
                        'selectAll',
                        'selectNone',
                        {
                            text: 'JSON Export',
                            action: function() {
                                if (table.rows({
                                        selected: true
                                    }).count() == 0) {
                                    $('#appMsg').html('<div class="alert alert-info">There is no selected rows.</div>');
                                } else {
                                    var export_data = [];
                                    table.rows({
                                        selected: true
                                    }).every(function() {
                                        export_data.push(this.data());
                                    });
                                    $.fn.dataTable.fileSave(
                                        new Blob([JSON.stringify(export_data, null, 4)]),
                                        'json_export.json'
                                    );
                                }
                            }
                        },
                        {
                            text: 'SHACL Export',
                            action: function() {
                                if (table.rows({
                                        selected: true
                                    }).count() == 0) {
                                    $('#appMsg').html('<div class="alert alert-info">There is no selected rows.</div>');
                                } else {
                                    var export_data = [];
                                    table.rows({
                                        selected: true
                                    }).every(function() {
                                        export_data.push(this.data());
                                    });
                                    shapes = shacl_export(export_data);
                                    $.fn.dataTable.fileSave(
                                        new Blob([shapes]),
                                        'shacl_export.ttl'
                                    );
                                }
                            }
                        }

                    ],
                    "pagingType": "simple_numbers",
                    "pageLength": 25,
                });
            }

            var html = '';
            var rules = new Object();
//            $.each(data, function(k, v) {
//                html += '<section class="popup-rule" id="popup-rule-' +
//                    v.ruleId + '" >' +
//                    '<pre>' + JSON.stringify(v, null, 4) + '</pre>' +
//                    '</section>';
//                rules[v.ruleId] = v;
//            });

            $('#rule-details').html(html);
            sessionStorage.rules = rules;

//            $(".popup-rule").dialog({
//                autoOpen: false,
//                resizable: false,
//                position: {
//                    my: "center top",
//                    at: "center",
//                    of: window
//                },
//                width: $(window).width() * 0.7,
//                height: 500,
//                open: function() {
//                    $('.ui-widget-overlay').addClass('custom-overlay');
//                },
//                title: "Rule detail",
//                show: {
//                    effect: "fade",
//                    duration: 100
//                },
//                hide: {
//                    effect: "fade",
//                    duration: 100
//                }
//            });


        },
        error: function(e) {

            var json = "<h4>Ajax Response</h4><pre>" + e.responseText +
                "</pre>";
            $('#resultsBlock').html(json);

            console.log("ERROR : ", e);
            $("#btn-search").prop("disabled", false);

        }
    });
}

function trim_prefix(str) {
    p1 = "http://dbpedia.org/ontology/";
    return str.replace(new RegExp(p1, 'g'), "");
}

function add_input_elm(idx, id, value) {
    var html = "<div>";
    html += "<form id='form-rule-edit-" + id + "' action='/api/rules/" + id + "/rating' method='put'>";
    if(idx.column == 3) {
    	if(value === null) value = 3;
    	html += "<input id='ejbeatycelledit' name='qualityEvaluation' type='number' placeholder='3' min='1' max='5' value='" + value + "'></input>";
    } else if (idx.column == 4) {
    	if(value === null) value = 0.0;
//    	html += "<form id='form-rule-edit-" + id + "' action='/api/rules/" + id + "' method='put'>";
    	html += "<input id='ejbeatycelledit' name='humanConfidence' type='number' step='0.01' min='0' max='1' " +
    			"maxlength='4' size='4' placeholder='0.0' value='" + value + "'></input>";
    }
    html += "<button type='submit' class='btn btn-primary btn-sm' value='OK'>OK</button>";
    html += "<button type='button' class='btn btn-secondary btn-sm' value='Cancel'>Cancel</button>";
    html += '<input type="hidden" name="rowIdx" value="' + idx.row + '">';
    html += '<input type="hidden" name="columnIdx" value="' + idx.column + '">';
    html += '</form>';
    html += "</div>";

    return html;
}

function shacl_export(data) {
	text = "";
	text += "@prefix dash: <http://datashapes.org/dash#> . \n" +
			"@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n" +
			"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n" +
			"@prefix schema: <http://schema.org/> . \n" +
			"@prefix sh: <http://www.w3.org/ns/shacl#> . \n" +
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . \n" +
			"@prefix shapes: <http://rudik.eurecom.fr/ruleshapes/> . \n\n";
	
	data.forEach(function(rule) {
		premise = rule.premise;
		atoms = premise.split("&");
		finish = false;
		path = "(";
		next_var = "";
		shape_contraint = "sh:property";
		compare_op = "";
		compare_path = "";
		
		literal = (premise.match(/[=,<,>]\(.+\)/gi) || []).length;
		
		if(rule.ruleType != true) {
			if(literal === 1) {
				path += "<" + rule.predicate + "> ";
				next_var = "object";
				
				atoms_count = atoms.length;
				j = 0;
				while(j < (atoms_count - 1)) {
					for(var i = 0; i < atoms.length; i++) {
						if(atoms[i].includes(next_var)) {
							atom = atoms.splice(i, 1).shift(); 
							atom = atom.trim();
							elements = atom.split(/(.+)\((.+)\,(.+)\)/g);
							if(elements[1].match(/[=,<,>]/gi) != null) {
								if(elements[2] == next_var) {
									switch(elements[1]) {
									case "<":
										shape_contraint = "sh:not";
										compare_op = "sh:lessThan";
										break;
									case "<=":
										shape_contraint = "sh:not";
										compare_op = "sh:lessThanOrEquals";
										break;
									case ">":
										compare_op = "sh:lessThanOrEquals";
										break;
									case ">=":
										compare_op = "sh:lessThan";
										break;
									case "=":
										compare_op = "sh:equals";
										break;
									case "!=":
										compare_op = "sh:disjoint";
										break;
									}
									next_var = elements[3];
								} else if(elements[3] == next_var) {
									switch(elements[1]) {
									case "<":
										compare_op = "sh:lessThanOrEquals";
										break;
									case "<=":
										compare_op = "sh:lessThan";
										break;
									case ">":
										shape_contraint = "sh:not";
										compare_op = "sh:lessThan";
										break;
									case ">=":
										shape_contraint = "sh:not";
										compare_op = "sh:lessThanOrEquals";
										break;
									case "=":
										compare_op = "sh:equals";
										break;
									case "!=":
										compare_op = "sh:disjoint";
										break;
									}
									next_var = elements[2];
								}
							} else {
								if(elements[2] == next_var) {
									path += "<" + elements[1] + "> ";
									next_var = elements[3];
								} else if(elements[3] == next_var) {
									path += "[sh:inversePath <" + elements[1] + ">] ";
									next_var = elements[2];
								}
							}
							j++;
							break;
						}
					};
				}
				
				//find the param of literal
				for(var i = 0; i < atoms.length; i++) {
					if(atoms[i].includes(next_var)) {
						atom = atoms.splice(i, 1).shift();
						atom = atom.trim();
						elements = atom.split(/(.+)\((.+)\,(.+)\)/g);
						if(elements[3] == next_var) {
							compare_path = "<" + elements[1] + "> ";
						} 
						break;
					}
				};
				
			} else if(literal == 0) {
				compare_path = "<" + rule.predicate + ">";
				compare_op = "sh:disjoint";
				next_var = "subject";
				//find the path
				while(next_var != "object") {
					for(var i = 0; i < atoms.length; i++) {
						if(atoms[i].includes(next_var)) {
							atom = atoms.splice(i, 1).shift();
							atom = atom.trim();
							elements = atom.split(/(.+)\((.+)\,(.+)\)/g);
							if(elements[2] == next_var) {
								path += "<" + elements[1] + "> ";
								next_var = elements[3];
							} else if(elements[3] == next_var) {
								path += "[sh:inversePath <" + elements[1] + ">] ";
								next_var = elements[2];
							}
							
							break;
						}
					};
				}
			}
		} else {
			
		}

		path += ")";
		
		shape = "\n" + "shapes:" + rule.ruleId + "\n";
		shape += "a sh:NodeShape;\n";
		shape += "rdfs:comment \"" + trim_prefix(premise) + " => " + trim_prefix(rule.predicate) + "\";\n";
		if(rule.ruleType == true) {
			shape += "sh:description \"Positive rule\" ;\n\n.\n"
		} else {
			shape += "sh:description \"Negative rule\" ;\n";
			shape += "sh:targetSubjectsOf <" + rule.predicate + ">;\n";
			shape += shape_contraint + " [\n";
			shape += "\t" + "sh:path " + path + ";\n";
			shape += "\t" + compare_op + " " + compare_path + ";\n";
			shape += "]\n\n";
			shape += ".\n"
		}
		
		text += shape;
	})
	
	return text;
}
