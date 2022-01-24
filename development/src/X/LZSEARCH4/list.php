<?php

require_once('for_php7.php');

    //���¥����å�
    $auth = common::SecurityCheck(STAFFCD, $model->programid);
    if ($auth != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }

    $objForm = new form;
    $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

    $db     = Query::dbCheckOut();

    //�����ܥ���
    $objForm->ae( array("type" 		=> "button",
                        "name"      => "SEARCH_BTN",
                        "value"     => "�����������",
                        "extrahtml" => "onclick=\"wopen('index.php?cmd=search_view','lzsearch',0,0,450,250);\""));

    $arg["SEARCH_BTN"] = $objForm->ge("SEARCH_BTN");

    //�������ɽ��
    if ($model->cmd == "search") {
        $result = $db->query(lzsearchQuery::SearchStudent($model->search_fields));
        $i =0;
        list($path, $cmd) = explode("?cmd=", $model->path);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             array_walk($row, "htmlspecialchars_array");
             $arg["data"][]   = $row;
             $i++;
        }
        $arg["RESULT"] = "��̡�".$i."̾";
        $result->free();
        if ($i == 0) {
            $arg["search_result"] = "SearchResult();";
        }
    }

    Query::dbCheckIn($db);

    //hidden(��������ͤ��Ǽ����)
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd") );
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "COMPANY_NAME") );
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SHUSHOKU_ADD") );

    $arg["finish"]  = $objForm->get_finish();

    if(VARS::post("cmd")==""){
        $arg["reload"] ="wopen('index.php?cmd=search_view','lzsearch',0,0,450,250);";
    }
    View::toHTML($model, "list.html", $arg);
?>
