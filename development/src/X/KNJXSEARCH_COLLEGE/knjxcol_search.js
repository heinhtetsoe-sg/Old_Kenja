function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function apply_school() {
    /* 学校名 *********************************************************************************/
    var school_list    = document.forms[0].SCHOOL_LIST;
    if (school_list.value) {
        var school_cd          = school_list.value.split('|')[0];
        var school_name        = school_list.value.split('|')[1];
        var school_group       = school_list.value.split('|')[2];
        var school_group_name  = school_list.value.split('|')[3];
        var zipcd              = school_list.value.split('|')[4];
        var addr1              = school_list.value.split('|')[5];
        var addr2              = school_list.value.split('|')[6];
        var telno              = school_list.value.split('|')[7];
        if (document.forms[0].FACULTY.value.split('|')[3]) {
            zipcd = document.forms[0].FACULTY.value.split('|')[2]
            addr1 = document.forms[0].FACULTY.value.split('|')[3]
            addr2 = document.forms[0].FACULTY.value.split('|')[4]
            telno = document.forms[0].FACULTY.value.split('|')[5]
        }
    } else {
        var school_cd      = "";
        var school_name    = "";
        var school_group   = "";
        var zipcd          = "";
        var addr1          = "";
        var addr2          = "";
        var telno          = "";
    }
    if (!school_cd) {
        school_cd = "";
    }
    if (!school_name) {
        school_name = "" ;
    }
    if (!school_group) {
        school_group = "";
    }
    if (!zipcd) {
        zipcd = "";
    }
    if (!addr1) {
        addr1 = "";
    }
    if (!addr2) {
        addr2 = "";
    }
    if (!telno) {
        telno = "";
    }

    /*************/
    /* SCHOOL_CD */
    /*************/
    var p_form_school_cd   = eval("parent.document.forms[0].SCHOOL_CD" + document.forms[0].target_number.value);
    if (p_form_school_cd) {
        p_form_school_cd.value = school_cd;
    }
    var p_div_school_cd   = eval("parent.document.getElementById('SCHOOL_CD" + document.forms[0].target_number.value + "')");
    if (p_div_school_cd) {
        if (p_div_school_cd.nodeName == 'DIV') {
            p_div_school_cd.innerHTML = school_cd;
        }
    }
    /***************/
    /* SCHOOL_NAME */
    /***************/
    var p_form_school_name   = eval("parent.document.forms[0].SCHOOL_NAME" + document.forms[0].target_number.value);
    if (p_form_school_name) {
        p_form_school_name.value = school_name;
    }
    var p_div_school_name   = eval("parent.document.getElementById('SCHOOL_NAME" + document.forms[0].target_number.value + "')");
    if (p_div_school_name) {
        if (p_div_school_name.nodeName == 'DIV') {
            p_div_school_name.innerHTML = school_name;
        }
    }
    /****************/
    /* SCHOOL_GROUP */
    /****************/
    if (school_group != "") {
        var p_form_school_group   = eval("parent.document.forms[0].SCHOOL_GROUP" + document.forms[0].target_number.value);
        if (p_form_school_group) {
            p_form_school_group.value = school_group;
        }
        var p_div_school_group   = eval("parent.document.getElementById('SCHOOL_GROUP" + document.forms[0].target_number.value + "')");
        if (p_div_school_group) {
            if (p_div_school_group.nodeName == 'DIV') {
                p_div_school_group.innerHTML = school_group;
            }
        }
    }
    /*********************/
    /* SCHOOL_GROUP_NAME */
    /*********************/
    var p_form_school_group_name   = eval("parent.document.forms[0].SCHOOL_GROUP_NAME" + document.forms[0].target_number.value);
    if (p_form_school_group_name) {
        p_form_school_group_name.value = school_group_name;
    }
    var p_div_school_group_name   = eval("parent.document.getElementById('SCHOOL_GROUP_NAME" + document.forms[0].target_number.value + "')");
    if (p_div_school_group_name) {
        if (p_div_school_group_name.nodeName == 'DIV') {
            p_div_school_group_name.innerHTML = school_group_name;
        }
    }
    /*********/
    /* ZIPCD */
    /*********/
    var p_form_zipcd   = eval("parent.document.forms[0].ZIPCD" + document.forms[0].target_number.value);
    if (p_form_zipcd) {
        p_form_zipcd.value = zipcd;
    }
    var p_div_zipcd   = eval("parent.document.getElementById('ZIPCD" + document.forms[0].target_number.value + "')");
    if (p_div_zipcd) {
        if (p_div_zipcd.nodeName == 'DIV') {
            p_div_zipcd.innerHTML = zipcd;
        }
    }
    /*********/
    /* ADDR1 */
    /*********/
    var p_form_addr1   = eval("parent.document.forms[0].ADDR1" + document.forms[0].target_number.value);
    if (p_form_addr1) {
        p_form_addr1.value = addr1;
    }
    var p_div_addr1   = eval("parent.document.getElementById('ADDR1" + document.forms[0].target_number.value + "')");
    if (p_div_addr1) {
        if (p_div_addr1.nodeName == 'DIV') {
            p_div_addr1.innerHTML = addr1;
        }
    }
    /*********/
    /* ADDR2 */
    /*********/
    var p_form_addr2   = eval("parent.document.forms[0].ADDR2" + document.forms[0].target_number.value);
    if (p_form_addr2) {
        p_form_addr2.value = addr2;
    }
    var p_div_addr2   = eval("parent.document.getElementById('ADDR2" + document.forms[0].target_number.value + "')");
    if (p_div_addr2) {
        if (p_div_addr2.nodeName == 'DIV') {
            p_div_addr2.innerHTML = addr2;
        }
    }
    /*********/
    /* TELNO */
    /*********/
    var p_form_telno   = eval("parent.document.forms[0].TELNO" + document.forms[0].target_number.value);
    if (p_form_telno) {
        p_form_telno.value = telno;
    }
    var p_div_telno   = eval("parent.document.getElementById('TELNO" + document.forms[0].target_number.value + "')");
    if (p_div_telno) {
        if (p_div_telno.nodeName == 'DIV') {
            p_div_telno.innerHTML = telno;
        }
    }

    /* 学部名 *********************************************************************************/
    var faculty        = document.forms[0].FACULTY;
    if (faculty.value) {
        var facultycd      = faculty.value.split('|')[0];
        var facultyname    = faculty.value.split('|')[1];
    } else {
        var facultycd        = "";
        var facultyname      = "";
    }
    if (!facultycd) {
        facultycd = "";
    }
    if (!facultyname) {
        facultyname = "";
    }

    /*************/
    /* FACULTYCD */
    /*************/
    var p_form_facultycd   = eval("parent.document.forms[0].FACULTYCD" + document.forms[0].target_number.value);
    if (p_form_facultycd) {
        p_form_facultycd.value = facultycd;
    }
    var p_div_facultycd   = eval("parent.document.getElementById('FACULTYCD" + document.forms[0].target_number.value + "')");
    if (p_div_facultycd) {
        if (p_div_facultycd.nodeName == 'DIV') {
            p_div_facultycd.innerHTML = facultycd;
        }
    }
    /***************/
    /* FACULTYNAME */
    /***************/
    var p_form_facultyname   = eval("parent.document.forms[0].FACULTYNAME" + document.forms[0].target_number.value);
    if (p_form_facultyname) {
        p_form_facultyname.value = facultyname;
    }
    var p_div_facultyname   = eval("parent.document.getElementById('FACULTYNAME" + document.forms[0].target_number.value + "')");
    if (p_div_facultyname) {
        if (p_div_facultyname.nodeName == 'DIV') {
            p_div_facultyname.innerHTML = facultyname;
        }
    }

    /* 学科名 *********************************************************************************/
    var department     = document.forms[0].DEPARTMENT;
    if (department.value) {
        var departmentcd   = department.value.split('|')[0];
        var departmentname = department.value.split('|')[1];
    } else {
        var departmentcd      = "";
        var departmentname    = "";
    }
    if (!departmentcd) {
        departmentcd = "";
    }
    if (!departmentname) {
        departmentname = "";
    }

    /****************/
    /* DEPARTMENTCD */
    /****************/
    var p_form_departmentcd   = eval("parent.document.forms[0].DEPARTMENTCD" + document.forms[0].target_number.value);
    if (p_form_departmentcd) {
        p_form_departmentcd.value = departmentcd;
    }
    var p_div_departmentcd   = eval("parent.document.getElementById('DEPARTMENTCD" + document.forms[0].target_number.value + "')");
    if (p_div_departmentcd) {
        if (p_div_departmentcd.nodeName == 'DIV') {
            p_div_departmentcd.innerHTML = departmentcd;
        }
    }
    /******************/
    /* DEPARTMENTNAME */
    /******************/
    var p_form_departmentname   = eval("parent.document.forms[0].DEPARTMENTNAME" + document.forms[0].target_number.value);
    if (p_form_departmentname) {
        p_form_departmentname.value = departmentname;
    }
    var p_div_departmentname   = eval("parent.document.getElementById('DEPARTMENTNAME" + document.forms[0].target_number.value + "')");
    if (p_div_departmentname) {
        if (p_div_departmentname.nodeName == 'DIV') {
            p_div_departmentname.innerHTML = departmentname;
        }
    }

    //東京都集計用
    if (document.forms[0].useTokyotoShinroTyousasyo.value == '1') {
        if (typeof parent.collegeSelectEvent3 == 'function') {
            parent.collegeSelectEvent3();
        }
    }
    parent.closeit();
}


