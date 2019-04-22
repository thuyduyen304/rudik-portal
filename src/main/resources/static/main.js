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
            params["qualityEvaluation"] = parseInt(this.elements["qualityEvaluation"].value);
            row_idx = parseInt(this.elements["rowIdx"].value);
            column_idx = parseInt(this.elements["columnIdx"].value);

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
                    cell_data = table.cell(row_idx, column_idx).data(modified_rule.qualityEvaluation).draw();
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
            rule_id = $(this).data('ruleId');
            $("#popup-rule-" + rule_id).dialog("open");
        });

        $("#results-table").on("click", "td.editable", function() {
            $(this).addClass('active');
            var table = $("#results-table").DataTable();
            cell_edit = this;
            idx = table.cell(this).index();
            row_data = table.rows(idx.row).data();

            form_elm = add_input_elm(idx, row_data[0].ruleId, row_data[0].qualityEvaluation);
            $(this).html(form_elm);
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
                        "data": "ruleType",
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
                                if (row.ruleType == true) {
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
                        "data": "qualityEvaluation",
                        className: "dt-center editable",
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
                        "data": "computedConfidence",
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
                                        'selected_export.json'
                                    );
                                }
                            }
                        },

                    ],
                    "pagingType": "simple_numbers",
                    "pageLength": 25,
                });
            }

            var html = '';
            var rules = new Object();
            $.each(data, function(k, v) {
                html += '<section class="popup-rule" id="popup-rule-' +
                    v.ruleId + '" >' +
                    '<pre>' + JSON.stringify(v, null, 4) + '</pre>' +
                    '</section>';
                rules[v.ruleId] = v;
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
    html += "<form id='form-rule-edit-" + id + "' action='/api/rules/" + id + "' method='put'>";
    html += "<input id='ejbeatycelledit' name='qualityEvaluation' type='number' min='1' max='5' value='" + value + "'></input>";
    html += "<button type='submit' class='btn btn-primary btn-sm' value='OK'>OK</button>";
    html += "<button type='button' class='btn btn-secondary btn-sm' value='Cancel'>Cancel</button>";
    html += '<input type="hidden" name="rowIdx" value="' + idx.row + '">';
    html += '<input type="hidden" name="columnIdx" value="' + idx.column + '">';
    html += '</form>';
    html += "</div>";

    return html;
}
