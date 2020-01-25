$(document).ready(
    function() {
    	var sPageURL = window.location.href;
		var ruleid = sPageURL.match(/rule_sample\/(.*)$/)[1];
    	if( $('#instance_samples #resultsBlock'))        
    	{
    		var msg = "<pre>Generating instances... Please wait</pre>";
            $('#appMsg').html(msg);
    		var hc = 1;
	    	$.ajax({
	    		type: "GET",
	            contentType: "application/json",
	            url: "/api/instances/rule-samples/" + ruleid,
	            dataType: 'json',
	            cache: false,
	            timeout: 600000,
	            success: function(data) {
	            	$('#appMsg').html('');
	            	$.each(data, function(i, inst) {
				        if (inst.label == -1) {
				        	hc = 0;
				        	return false;
				        }
				    });
//	            	if (hc == 0) {
//	            		$('#compute_confidence').html('<button type="submit" disabled id="btn-compute-hc" class="btn btn-primary btn-sm">Compute Human Confidence</button>');
//	            	}
//	            	else {
	            		$('#compute_confidence').html('<button type="submit" id="btn-compute-hc" class="btn btn-primary btn-sm">Compute Human Confidence</button>');
//	            	}
	            	
	            	$('#appMsg').html('');
	                $('#resultsBlock').show();

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
	                        "columns": [
	                        {
	                            "data": "premise",
	                            "render": function(data, type, row, meta) {
	                                if (type === 'display') {
	                                    if (row.ruleType == true) {
	                                        data = trim_prefix(row.premise) + ' \u21D2 ' +
	                                            trim_prefix(row.conclusion);
	                                    } else {
	                                        data = trim_prefix(row.premise) + ' \u0026	' +
	                                            trim_prefix(row.conclusion) + ' \u21D2 ' +
	                                            ' \u22A5 ';
	                                    }
	                        
	                                }

	                                return data;
	                            }
	                        }, {
	                            "data": "label",
	                            className: "dt-center editable",
	                            "render": function(data, type, row, meta) {
	                                if (type === 'display') {
	                                    data = '<div data-toggle="tooltip" data-placement="top" title="Click to edit">' +
	                                    	'<a href="javascript:void(0)" >' + data + '</a>' +
	                                    	'</div>';
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
	                            

	                        ],
	                        "pagingType": "simple_numbers",
	                        "pageLength": 25,
	                    });
	                }

	            },
	            error: function(httpObj, textStatus) {       
	                if(httpObj.status==401 || httpObj.status==403)
	                	location.href='/login';
	                else {
	                	$('#appMsg').html('Error. Please try again.');
	                }
	            }
	    	});
    	}
    	
    	$("#results-table").on("submit", ".editable form", function(e) {
            e.preventDefault();
            
            var params = {}
            
            row_idx = parseInt(this.elements["rowIdx"].value);
            column_idx = parseInt(this.elements["columnIdx"].value);
            if(column_idx == 1) {
            	params["label"] = parseInt(this.elements["label"].value);
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
                	if(column_idx == 1) {
                		cell_data = table.cell(row_idx, column_idx).data(modified_rule.label).draw();
                    } 
                    
                },
                error: function(e) {
                    cell_data = table.cell(row_idx, column_idx).data();
                    table.cell(row_idx, column_idx).data(cell_data).draw();
                    $('#appMsg').html('<div class="alert alert-info">Error. Cannot update rule.</div>');
                }
            });
        });
    	
    	$("#compute_confidence").on("click", function(e) {
            e.preventDefault();
            $.ajax({
                type: "PUT",
                contentType: "application/json",
                url: "/api/instances/compute_hc/" + ruleid,
                dataType: 'json',
                cache: false,
                timeout: 600000,
                success: function(modified_rule) {
                	$(".human_confidence").html(modified_rule); 
                    
                },
                error: function(e) {
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
            form_elm = add_input_elm(idx, row_data[0].instanceId, value);
            $(this).html(form_elm);
        });
        

        $("#results-table").on("click", "form", function(e) {
            e.stopPropagation();
        });
    	

    });

function add_input_elm(idx, id, value) {
    var html = "<div>";
    html += "<form id='form-rule-edit-" + id + "' action='/api/instances/" + id + "' method='put'>";
    if(idx.column == 1) {
    	
    	html += "<input id='ejbeatycelledit' name='label' type='number' placeholder='3' min='0' max='1' value='" + value + "'></input>";
    } 
    html += "<button type='submit' class='btn btn-primary btn-sm' value='OK'>OK</button>";
    html += "<button type='button' class='btn btn-secondary btn-sm' value='Cancel'>Cancel</button>";
    html += '<input type="hidden" name="rowIdx" value="' + idx.row + '">';
    html += '<input type="hidden" name="columnIdx" value="' + idx.column + '">';
    html += '</form>';
    html += "</div>";

    return html;
}

function trim_prefix(str) {
    p1 = "http://dbpedia.org/ontology/";
    p2 = "http://dbpedia.org/resource/";
//    p3 = "http://www.wikidata.org/prop/direct/";
    p4 = "http://yago-knowledge.org/resource/";
    return str.replace(new RegExp(p1 + '|' + p2 + '|' + p4, 'g'), "");
}