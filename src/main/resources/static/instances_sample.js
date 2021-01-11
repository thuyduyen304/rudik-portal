$(document).ready(
    function() {
		var ruleid = new URL(document.URL).pathname.split("/")[2]
    	if( $('#rule-instance-samples'))        
    	{
    		var msg = "<pre>Generating instances... Please wait</pre>";
            $('#appMsg').html(msg);
    		var hc = 1;
	    	$.ajax({
	    		type: "GET",
	            contentType: "application/json",
	            url: "/api/rules/" + ruleid + "/sample-instances",
	            dataType: 'json',
	            cache: false,
	            timeout: 600000,
	            success: function(data) {
	            	$('#appMsg').html('');
//	            	$.each(data, function(i, inst) {
//				        if (inst.label == -1) {
//				        	hc = 0;
//				        	return false;
//				        }
//				    });
	            	
	            	$('#appMsg').html('');
	                $('#rule-instance-samples').show();

	                if ($.fn.dataTable.isDataTable("#results-table")) {
	                    var table = $("#results-table").DataTable();
	                    table.clear();
	                    table.rows.add(data);
	                    table.draw();
	                } else {
	                    var table = $('#results-table').DataTable({
	                        fixedHeader: true,
	                        data: data,
	                        "columns": [
	                        	{
	                                data: null,
	                                defaultContent: '',
	                                className: 'select-checkbox',
	                                orderable: false
	                            },
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
	                                    data = '<a class="popup-opener" data-instance-id="' + row.instanceId + '" >' + data + '</a>';
	                        
	                                }

	                                return data;
	                            }
	                        }, {
	                            "data": "label",
	                            className: "dt-center editable",
	                            "render": function(data, type, row, meta) {
	                            	label_text = "NaN";
	                            	if (data == 1) {
	                            		label_text = "True";
	                            	} else if (data == 0){
	                            		label_text = "False";
	                            	}
	                                if (type === 'display') {
	                                    data = '<div data-toggle="tooltip" data-placement="top" title="Click to edit">' +
	                                    	'<a href="javascript:void(0)" >' + label_text + '</a>' +
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
	                            'selectAll',
	                            'selectNone',
	                            {
	                                text: 'JSON Export',
	                                action: function() {
	                                    if (table.rows({
	                                            selected: true
	                                        }).count() == 0) {
	                                        $('#appMsg').html('<div class="alert alert-info">There is no selected rows.</div>');
	                                        window.scrollTo(0, 0); 
	                                    } else {
	                                        var export_data = [];
	                                        table.rows({
	                                            selected: true
	                                        }).every(function() {
	                                            export_data.push(this.data());
	                                        });
	                                        $.fn.dataTable.fileSave(
	                                            new Blob([JSON.stringify(export_data, null, 4)]),
	                                            ruleid + '_sample_instances.json'
	                                        );
	                                    }
	                                }
	                            }
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
	                	window.scrollTo(0, 0); 
	                }
	            }
	    	});
    	}
    	
    	$("#results-table").on("submit", ".editable form", function(e) {
            e.preventDefault();
            
            var params = {}
            
            row_idx = parseInt(this.elements["rowIdx"].value);
            column_idx = parseInt(this.elements["columnIdx"].value);
            if(column_idx == 2) {
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
                	if(column_idx == 2) {
                		cell_data = table.cell(row_idx, column_idx).data(modified_rule.label).draw();
                    } 
                    
                },
                error: function(e) {
                    cell_data = table.cell(row_idx, column_idx).data();
                    table.cell(row_idx, column_idx).data(cell_data).draw();
                    $('#appMsg').html('<div class="alert alert-info">Error. Only admin can edit this field.</div>');
                    window.scrollTo(0, 0); 
                }
            });
        });
    	
    	$("#compute_confidence").on("click", function(e) {
            e.preventDefault();
            $.ajax({
                type: "PUT",
                contentType: "application/json",
                url: "/api/rules/" + ruleid + "/sample-instances/human-confidence",
                dataType: 'json',
                cache: false,
                timeout: 600000,
                success: function(modified_rule) {
                	$(".human_confidence").html(modified_rule); 
                    
                },
                error: function(e) {
                    $('#appMsg').html('<div class="alert alert-info">Error. ' + e.responseText + '.</div>');
                    window.scrollTo(0, 0); 
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
            instance_id = $(this).data('instanceId');
            $("#popup-rule-" + instance_id).dialog("open");
        });

        $("#results-table").on("click", "td.editable", function() {
            $(this).addClass('active');
            var table = $("#results-table").DataTable();
            cell_edit = this;
            idx = table.cell(this).index();
            row_data = table.rows(idx.row).data();
            value = table.cell(this).data(); 
            form_elm = add_input_elm(idx, row_data[0].instanceId, value);
            $(this).html(form_elm);
        });
        

        $("#results-table").on("click", "form", function(e) {
            e.stopPropagation();
        });
    	
        $("#results-table").on("click", "a.popup-opener", function() {
        	$('#rule-details').html('');
            instance_id = $(this).data('instanceId');
            $.ajax({
                type: "GET",
                contentType: "application/json",
                url: "/api/instances/" + instance_id,
                dataType: 'json',
                cache: false,
                timeout: 600000,
                success: function(data) {
		            html = '<section class="popup-rule" id="popup-rule-' + instance_id + '" >' +
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
    	                title: "Instance Details",
    	                show: {
    	                    effect: "fade",
    	                    duration: 100
    	                },
    	                hide: {
    	                    effect: "fade",
    	                    duration: 100
    	                }
    	            });
		            $("#popup-rule-" + instance_id).dialog("open");
                },
                error: function(e) {
                	var json = "<h4>Ajax Response</h4><pre>" + e.responseText + "</pre>";
		            $('#appMsg').html(json);
		            window.scrollTo(0, 0); 
                }
            });
        });
    });

function add_input_elm(idx, id, value) {
    var html = "<div>";
    html += "<form id='form-rule-edit-" + id + "' action='/api/instances/" + id + "' method='put'>";
    if(idx.column == 2) {
    	html += '<input type="radio" id="label-false" name="label" value="0"/><label for="label-false">False</label><br>' +
    		'<input type="radio" id="label-true" name="label" value="1"/><label for="label-true">True</label><br>';
//    	html += "<input id='ejbeatycelledit' name='label' type='number' placeholder='3' min='0' max='1' value='" + value + "'></input>";
    } 
    html += "<button type='submit' class='btn btn-primary btn-sm' value='OK'>OK</button>";
    html += "<button type='button' class='btn btn-secondary btn-sm' value='Cancel'>Cancel</button>";
    html += '<input type="hidden" name="rowIdx" value="' + idx.row + '">';
    html += '<input type="hidden" name="columnIdx" value="' + idx.column + '">';
    html += '</form>';
    html += "</div>";

    return html;
}
