function selectTyousasyo() {
    $("input:checked").each(function (index, element) {
        var name = $(element).attr("name").replace("CHECK_", "").replace("[]", "");
        var value = $(element).val();

        top.main_frame.right_frame.document.forms[0][name].value = value;
    });
    parent.closeit();
}
