<?php
class knjl002vForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl002vindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度コンボ
        $query = knjl002vQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->leftYear, $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);

        //校種コンボ
        $extra = "onChange=\"return btn_submit('list')\"";
        $query = knjl002vQuery::getSchoolKind($model->leftYear);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->schoolKind, $extra, 1);

        //リスト作成
        $result = $db->query(knjl002vQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $examId = $row["EXAM_SCHOOL_KIND"].$row["APPLICANT_DIV"].$row["COURSE_DIV"].$row["FREQUENCY"];
            $hash = array(
                "cmd"            => "edit",
                "EXAM_ID"        => $examId,
             );
            $row["EXAM_ID"] = View::alink("knjl002vindex.php", $examId, "target=\"right_frame\"", $hash);
            $arg["data"][] = $row;
        }
    
        $result->free();
    
        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $flg = in_array($model->cmd, array("list", "copy"));
        if (!isset($model->warning) && $flg) {
            $reload  = "parent.right_frame.location.href='knjl002vindex.php?cmd=edit";
            $reload .= "&YEAR=".$model->leftYear;
            $reload .= "&EXAM_SCHOOL_KIND=".$model->schoolKind."'";
            $arg["reload"] = $reload;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl002vForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
