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
            	params["quality_evaluation"] = parseInt(this.elements["qualityEvaluation"].value);
            } else if(column_idx == 4) {
            	params["human_confidence"] = parseFloat(this.elements["humanConfidence"].value);
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
                	if(column_idx == 3) {
                		cell_data = table.cell(row_idx, column_idx).data(modified_rule.quality_evaluation).draw();
                    } else if(column_idx == 4) {
                    	cell_data = table.cell(row_idx, column_idx).data(modified_rule.human_confidence).draw();
                    }
                    
                },
                error: function(e) {
                    cell_data = table.cell(row_idx, column_idx).data();
                    table.cell(row_idx, column_idx).data(cell_data).draw();
                    $('#appMsg').html('<div class="alert alert-info">Error. Cannot update rule.</div>');
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
        	$('#rule-details').html('');
            rule_id = $(this).data('ruleId');
            $.ajax({
                type: "GET",
                contentType: "application/json",
                url: "/api/rules/" + rule_id,
                dataType: 'json',
                cache: false,
                timeout: 600000,
                success: function(data) {
		            html = '<section class="popup-rule" id="popup-rule-' + rule_id + '" >' +
	                '<pre>' + JSON.stringify(data, null, 4) + '</pre>' +
	                '</section>';
		            $('#rule-details').html(html);
		            $(".popup-rule").dialog({
    	                autoOpen: false,
    	                resizable: false,
    	                position: {
    	                    my: "center top",
    	                    at: "center",
    	                    of: window
    	                },
    	                width: $(window).width() * 0.7,
    	                height: 500,
    	                open: function() {
    	                    $('.ui-widget-overlay').addClass('custom-overlay');
    	                },
    	                title: "Rule Details",
    	                show: {
    	                    effect: "fade",
    	                    duration: 100
    	                },
    	                hide: {
    	                    effect: "fade",
    	                    duration: 100
    	                }
    	            });
		            $("#popup-rule-" + rule_id).dialog("open");
                },
                error: function(e) {
                	var json = "<h4>Ajax Response</h4><pre>" + e.responseText + "</pre>";
		            $('#appMsg').html(json);
                }
            });
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
        
        $("#results-table").on("click", "td.dt-status a.rule-op", function() {
            var table = $("#results-table").DataTable();
            idx = table.cell(this.parentNode).index();
            row_data = table.rows(idx.row).data();
            var params = {}
            params = row_data[0].status;
            
            $.ajax({
                type: "PUT",
                contentType: "application/json",
                url: "/api/rules/approve/" + row_data[0].ruleId,
                data: JSON.stringify(params),
                dataType: 'json',
                cache: false,
                timeout: 600000,
                success: function(modified_rule) {
                	if (modified_rule.status == true) {
                		table.cell(idx.row, idx.column).data(true);
                	}
                	else if (modified_rule.status == false){
                		table.cell(idx.row, idx.column).data(false);
                	}
                },
                error: function(e) {
                	console.log("ERROR : ", e);
                    $('#appMsg').html('<div class="alert alert-info">Error. Cannot update rule.</div>');
                }
            });

        });

        $("#results-table").on("click", "form", function(e) {
            e.stopPropagation();
        });

    });

function search_rules_submit() {

    var search = {}
    search["knowledgeBase"] = $("#knowledgeBase").val();
    search["predicate"] = $("#predicate").val();
    search["ruleType"] = $("#ruleType").val();
    search["ruleStatus"] = $("#ruleStatus").val();
    search["humanConfidenceFrom"] = $("#humanConfidenceFrom").val();
    search["humanConfidenceTo"] = $("#humanConfidenceTo").val();

    $("#btn-search").prop("disabled", true);

    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/api/rules/rule-approve",
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
                                data = '<div data-toggle="tooltip" data-placement="top" title="Click to edit">' +
                                '<a href="javascript:void(0)" onclick="displayVotes(\'' + row.ruleId + '\');" >' + data + '</a>' +
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
                                	data + '</div>';
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
                        "data": "status",
                        className: "dt-status",
                        "render": function(data, type, row, meta) {
                        	if (type === 'display') {
                            	if (data == true) {
                            		data = '<a class="rule-op btn btn-sm btn-cancel" role="button">Cancel</a>';
                                } else {
                                	data = '<a class="rule-op btn btn-sm" role="button">Approve</a>';
                                }
                            	data += '<a class="btn btn-sm btn-info btn-eval" role="button" target="_blank" href="/rule_sample/' + row.ruleId + '">Evaluate</a>';
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
                            text: 'Approve Rules',
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
                                        export_data.push(this.data().ruleId);
                                    });
                                    $.ajax({
                                        type: "PUT",
                                        contentType: "application/json",
                                        url: "/api/rules/change-status/true",
                                        data: JSON.stringify(export_data),
                                        dataType: 'json',
                                        cache: false,
                                        timeout: 600000,
                                        success: function(modified_rule) {
                                        	table.rows({
                                                selected: true
                                            }).every(function( rowIdx, tableLoop, rowLoop) {
                                            	if (modified_rule.status == true) {
                                            		table.cell(rowIdx, 6).data(true);
                                            	}
                                            });
                                        	
                                        },
                                        error: function(e) {
                                        	console.log("ERROR : ", e);
                                            $('#appMsg').html('<div class="alert alert-info">Error. Cannot update rule.</div>');
                                        }
                                    });
                                }
                            }
                        },
                        {text: 'Cancel Rules',
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
                                        export_data.push(this.data().ruleId);
                                    });
                                    $.ajax({
                                        type: "PUT",
                                        contentType: "application/json",
                                        url: "/api/rules/change-status/false",
                                        data: JSON.stringify(export_data),
                                        dataType: 'json',
                                        cache: false,
                                        timeout: 600000,
                                        success: function(modified_rule) {
                                        	table.rows({
                                                selected: true
                                            }).every(function( rowIdx, tableLoop, rowLoop) {
                                            	if (modified_rule.status == false) {
                                            		table.cell(rowIdx, 6).data(false);
                                            	}
                                            });
                                        	
                                        },
                                        error: function(e) {
                                        	console.log("ERROR : ", e);
                                            $('#appMsg').html('<div class="alert alert-info">Error. Cannot update rule.</div>');
                                        }
                                    });
                                }
                            }
                        }

                    ],
                    "pagingType": "simple_numbers",
                    "pageLength": 25,
                });
            }

//            var html = '';
//            var rules = new Object();
//            $.each(data, function(k, v) {
//                html += '<section class="popup-rule" id="popup-rule-' +
//                    v.ruleId + '" >' +
//                    '<pre>' + JSON.stringify(v, null, 4) + '</pre>' +
//                    '</section>';
//                rules[v.ruleId] = v;
//            });

//            $('#rule-details').html(html);
//            sessionStorage.rules = rules;

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
        error: function(httpObj, textStatus) {       
            if(httpObj.status==401 || httpObj.status==403)
            	location.href='/login';
            else {
            	var json = "<h4>Ajax Response</h4><pre>" + e.responseText +
                "</pre>";
	            $('#resultsBlock').html(json);
	
	            console.log("ERROR : ", e);
	            $("#btn-search").prop("disabled", false);
            }
        }
    });
}

function trim_prefix(str) {
    p1 = "http://dbpedia.org/ontology/";
    p2 = "http://yago-knowledge.org/resource/";
    p3 = "http://www.wikidata.org/prop/direct/";
    return str.replace(new RegExp(p1 + '|' + p2 + '|' + p3, 'g'), "");
}

function add_input_elm(idx, id, value) {
    var html = "<div>";
    html += "<form id='form-rule-edit-" + id + "' action='/api/rules/" + id + "' method='put'>";
    if(idx.column == 3) {
    	if(value == null)
    		value = 3;
    	html += "<input id='ejbeatycelledit' name='qualityEvaluation' type='number' placeholder='3' min='1' max='5' value='" + value + "'></input>";
    } else if (idx.column == 4) {
    	if(value == null)
    		value = 0.0;
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

function displayVotes(rule_id) {
	$.ajax({
        type: "GET",
        contentType: "application/json",
        url: "/api/rules/" + rule_id + "/rating",
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function(votes) {
//        	console.log("test");
//        	console.log(votes);
//        	text = "<div><h3>Users votes:</h3>"
//        	votes.forEach(function(item) {
//        		text += "<div> Rating " + item.rating + ": " + item.total + " vote(s)</div>";
//        	});
//        	text += "</div>";
//        	var w = window.open('', '', 'width=400,height=200,resizeable,scrollbars');
//            w.document.write(text);
//            w.document.close(); // needed for chrome and safari
        	$('#rule-details').html('');
        	if (votes.length > 0) {
        		text = "<div>Number of votes corresponding to each rating:</div>"
                	votes.forEach(function(item) {
                		text += "<div> Rating " + item.rating + ": " + item.total + " vote(s)</div>";
                	});
                	text += "</div>"
        	} else {
        		text = "<div>This rule has no votes.</div>"
        	}
        	
            html = '<section style="color:#333333" class="popup-rule" id="popup-rule-' + rule_id + '-vote" >' +
            text + '</section>';
            console.log(html);
            $('#rule-details').html(html);
            $("#popup-rule-" + rule_id + "-vote").dialog({
                autoOpen: false,
                resizable: false,
                position: {
                    my: "center top",
                    at: "center",
                    of: window
                },
                width: $(window).width() * 0.4,
                height: 200,
                open: function() {
                    $('.ui-widget-overlay').addClass('custom-overlay');
                },
                title: "Users' Votes",
                show: {
                    effect: "fade",
                    duration: 100
                },
                hide: {
                    effect: "fade",
                    duration: 100
                }
            });
            $("#popup-rule-" + rule_id + "-vote").dialog("open");
        },
        error: function(e) {
        	console.log("ERROR : ", e);
            $('#appMsg').html('<div class="alert alert-info">Error. Cannot get votes.</div>');
        }
    });
	
}
