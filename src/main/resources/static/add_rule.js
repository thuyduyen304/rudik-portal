$(document).ready(
    function() {
        $('#add-form #knowledgeBase')
            .change(
            		function() {
                        knowledge_base = $("#knowledgeBase").val();
                        $
                            .ajax({
                                type: "GET",
                                contentType: "application/json",
                                url: "/api/rules/all-predicates",
                                dataType: 'json',
                                cache: false,
                                timeout: 600000,
                                success: function(data) {
                                	console.log(data);
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

            // stop submit the form, we will post it manually.
            event.preventDefault();

            search_rules_submit();

        });
        
        // Add premise.
        var next = 0
        $(document).on('click', '.btn-add', function(e) {
	        e.preventDefault();
	        
	        var controlForm = $('.controls .input-premise:first');
	        var currentEntry = $(this).parents('.entry:first'),
	            newEntry = $(currentEntry.clone()).appendTo(controlForm);

	        newEntry.find('input').val('');
	        controlForm.find('.entry:not(:last) .btn-add')
	            .removeClass('btn-add').addClass('btn-remove')
	            .removeClass('btn-success').addClass('btn-danger')
	            .html('<span class="glyphicon glyphicon-minus"></span>');
	    }).on('click', '.btn-remove', function(e) {
			$(this).parents('.entry:first').remove();

			e.preventDefault();
			return false;
		});
        $("#add-form :input").change(function() {
        	$("#btn-add-rule").prop("disabled", true);
    	});
        $("#add-form").submit(function(event) {

            // stop submit the form, we will post it manually.
            event.preventDefault();

            add_rule_submit();

        });
        
        $("#btn-check-score").click(function(event) {
            var add = validation();
            if (typeof add === 'object'){
            	$('#form-error').html('');
            	$.ajax({
        	        type: "POST",
        	        contentType: "application/json",
        	        url: "/api/rules/get-score",
        	        data: JSON.stringify(add),
        	        dataType: 'json',
        	        cache: false,
        	        timeout: 600000,
        	        success: function(data) {
        	        	$(".popup-rule").dialog('destroy').remove();
        	            $("#btn-add-rule").prop("disabled", false);
        	            var status = Object.keys(data[0])[0];
        	            if (status == 'exist') {
        	            	var title = "Rule is exist. Adding rule is not successfully";
        	            }
        	            else if (status == 'invalid') {
        	            	var title = "Premise is wrong format.";
        	            }
        	            else {
        	            	$("#resultMessages").css("display", "block");
        	            	$(".resultMessages").text(status);
        	            }
        	            var html = '';
        	            if (status == 'exist'){
        		            $('#rule-details').html('');
        		            html = '<section class="popup-rule" id="popup-rule" >' +
        	                '<pre>' + JSON.stringify(data[0][status], null, 4) + '</pre>' +
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
            	                title: title,
            	                show: {
            	                    effect: "fade",
            	                    duration: 100
            	                },
            	                hide: {
            	                    effect: "fade",
            	                    duration: 100
            	                }
            	            });
            	            $("#popup-rule").dialog("open");
        	            }
        	            else if (status == 'invalid'){
        	            	$('#form-error').html('<ul class="errorMessages fa fa-warning" style="display: block;">' + 
        	            			 'Premise is wrong format.</ul>');
        	            }
        	                   	          	        
        	        },
        	        error: function(e) {
        	
        	            var json = "<h4>Ajax Response</h4><pre>" + e.responseText +
        	                "</pre>";
        	            $('#resultsBlock').html(json);
        	
        	            console.log("ERROR : ", e);
        	            $("#btn-add-rule").prop("disabled", false);
        	
        	        }
        	    });
            	           	
            }
            else{
            	$('#form-error').html('<ul class="errorMessages fa fa-warning" style="display: block;">' + add +'</ul>');
            }

        });

    });

function add_rule_submit() {
	var add = validation();
    if (typeof add === 'object') { 
    	$('#form-error').html('');
	    $("#btn-add").prop("disabled", true);
	    $.ajax({
	        type: "POST",
	        contentType: "application/json",
	        url: "/api/rules/add-rule",
	        data: JSON.stringify(add),
	        dataType: 'json',
	        cache: false,
	        timeout: 600000,
	        success: function(data) {
	        	$(".popup-rule").dialog('destroy').remove();
	            $("#btn-add-rule").prop("disabled", false);
	            var status = Object.keys(data[0])[0];
	            if (status == 'exist') {
	            	var title = "Rule is exist. Adding rule is not successfully";
	            }
	            else if (status == 'invalid') {
	            	var title = "Premise is wrong format.";
	            }
	            else {
	            	var title = "Rule is added successfully.";
	            }
	            var html = '';
	            if (status != 'invalid'){
		            $('#rule-details').html('');
		            html = '<section class="popup-rule" id="popup-rule" >' +
	                '<pre>' + JSON.stringify(data[0][status], null, 4) + '</pre>' +
	                '</section>';
		            $('#rule-details').html(html);
	            }
	            else{
	            	$('#rule-details').html('');
		            html = '<section class="popup-rule" id="popup-rule" >' +
	                '<pre>Example: http://dbpedia.org/ontology/birthDate(object,v0) & =(v0,v1)</pre>' +
	                '</section>';
		            $('#rule-details').html(html);
	            }

	            
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
	                title: title,
	                show: {
	                    effect: "fade",
	                    duration: 100
	                },
	                hide: {
	                    effect: "fade",
	                    duration: 100
	                }
	            });
	            $("#popup-rule").dialog("open");
	            
	
	        },
	        error: function(e) {
	
	            var json = "<h4>Ajax Response</h4><pre>" + e.responseText +
	                "</pre>";
	            $('#resultsBlock').html(json);
	
	            console.log("ERROR : ", e);
	            $("#btn-add-rule").prop("disabled", false);
	
	        }
	    });
    }
    else {
	    $('#form-error').html('<ul class="errorMessages fa fa-warning" style="display: block;">' + add +'</ul>');
	}   

     
}

function validation() {
	var message = '';
    var add = {}
    add["knowledgeBase"] = $("#knowledgeBase").val();
    add["predicate"] = $("#predicate").val();
    add["ruleType"] = $("#ruleType").val() == 1 ? true : false;
    add["premise"] = $("#premise").val();
    add["humanConfidence"] = $("#humanConfidence").val();
    add["qualityEvaluation"] = $("#qualityEvaluation").val();
    add["computedConfidence"] = $("#computedConfidence").text();

    if (add["knowledgeBase"] == 'none') {    	
    	message += '<li>Choose the Knowledge Base.</li>';
    }
    if (add["predicate"] == 'none') {
    	message += '<li>Choose the Predicate.</li>';
    }
    if (add["ruleType"] == -1) {
    	message += '<li>Choose the Rule Type.</li>';
    }
    if (add["premise"] == '') {
    	message += '<li>Fill the Premise.</li>';
    }
    
//    var pre = add["premise"].split('&');
//    for (int j = 0; j < pre.length; j++) {
//    	pre[j].trim();
//    }
    
    if (add["qualityEvaluation"] == '') {
    	message += '<li>Fill the Quality Evaluation.</li>';
    }
    if (add["humanConfidence"] == '') {
    	message += '<li>Fill the Human Confidence.</li>';
    }
    if (!(add["humanConfidence"] >= 0 && add["humanConfidence"] <=1)) {
    	message += '<li>Human Confidence belongs [0,1].</li>';
    }
    var quality_arr = ['1', '2', '3', '4', '5'];
    if (quality_arr.includes(add["qualityEvaluation"]) == false) {
    	message += '<li>Quality Evaluation belongs 1, 2, 3, 4, 5.</li>';
    }
    
    if (message != ''){
    	return message;
    }
    else {
    	return add;
    }
}

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
        url: "/api/rule",
        data: JSON.stringify(search),
        dataType: 'json',
        cache: false,
        timeout: 600000,
        success: function(data) {
            $('#resultsBlock').show();
            $("#btn-search").prop("disabled", false);
            
            var html = '';
            var rules = new Object();

            $.each(data, function(k, v) {
                html += '<section class="popup-rule" id="popup-rule-' +
                	v.ruleId + '" >' +
                    '<pre>' + JSON.stringify(v, null, 4) + '</pre>' +
                    '</section>';
                rules[v.ruleId] = v;

                if (v.ruleType == true) {
                    data[k].content = trim_prefix(v.premise) + ' \u21D2 ' +
                        trim_prefix(v.conclusion);
                    data[k].type = '+';
                } else {
                    data[k].content = trim_prefix(v.premise) + ' \u0026	' +
                        trim_prefix(v.conclusion) + ' \u21D2 ' +
                        ' \u22A5 ';
                    data[k].type = '-';
                }
            });

            $('#results-table').DataTable({
            	destroy:true,
                data: data,
                "columns": [{
                    data: null,
                    defaultContent: '',
                    className: 'select-checkbox',
                    orderable: false
                }, {
                    "data": "type",
                    className: "dt-center"
                }, {
                    "data": "content",
                    "render": function(data, type, row, meta) {
                        if (type === 'display') {
                            data = '<a class="popup-opener" data-rule-id="' + row.ruleId + '" href="#">' + data + '</a>';
                        }

                        return data;
                    }
                }, {
                    "data": "humanConfidence",
                    className: "dt-center",
                    "render": function(data, type, row, meta) {
                        if (type === 'display') {
                            data = '<a href="/instance/sample?rule_id=' + row.ruleId + '" >' + data + '</a>';
                        }

                        return data;
                    }
                }, {
                    "data": "humanConfidence",
                    className: "dt-center"
                }, {
                    "data": "humanConfidence",
                    className: "dt-center"
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
                "pagingType": "simple_numbers",
                "pageLength": 25,
            });

            $('#rule-details').html(html);
            sessionStorage.rules = rules;

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
                title: "Rule detail",
                show: {
                    effect: "fade",
                    duration: 100
                },
                hide: {
                    effect: "fade",
                    duration: 100
                }
            });
            $(".popup-opener").on("click", function() {
                $("#popup-rule-" + $(this).data("ruleId")).dialog("open");
            });


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