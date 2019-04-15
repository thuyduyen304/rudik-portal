$(document).ready(
    function() {
        $('#search-form #knowledgeBase')
            .change(
                function() {
                    console.log("test" +
                        $("#knowledgeBase").val());

                    $
                        .ajax({
                            type: "POST",
                            contentType: "application/json",
                            url: "/api/rule/predicates",
                            data: {
                                knowledge_base: $(
                                        "#knowledgeBase")
                                    .val()
                            },
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

            // stop submit the form, we will post it manually.
            event.preventDefault();

            search_rules_submit();

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