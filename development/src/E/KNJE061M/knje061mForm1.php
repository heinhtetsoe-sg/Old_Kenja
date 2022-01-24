<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje061mForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){ $arg["jscript"] = "OnAuthError();";}

        $db = Query::dbCheckOut();
        $objForm = new form;
        
        //年度学期表示
        $arg["SEMESTERNAME"] = CTRL_YEAR ."年度　" .CTRL_SEMESTERNAME;

        //学年
        $gradeFlg = false;
        $model->regdGdat = array();
        $result = $db->query(knje061mQuery::selectQueryAnnual($model));
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            $model->regdGdat[$row["VALUE"]]["SCHOOL_KIND"]  = $row["SCHOOL_KIND"];
            $model->regdGdat[$row["VALUE"]]["GRADE_CD"]     = $row["GRADE_CD"];
            if ($model->annual == $row["VALUE"]) $gradeFlg = true;
        }
        if (!isset($model->annual) || !$gradeFlg) $model->annual = $opt[0]["value"];
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('annual');\"",
                            "options"    => $opt));
        
        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        $result = $db->query(knje061mquery::selectQueryHRClass($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["HR_NAME"],
                           "value" => $row["HR_CLASS"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->hr_class,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('hr_class');\"",
                            "options"    => $opt));

        $arg["HR_CLASS"] = $objForm->ge("HR_CLASS");
        
        //コース
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        $value_flg = false;
        $result = $db->query(knje061mquery::selectCourse($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECODE"]."：".$row["COURSECODENAME"],
                           "value" => $row["COURSECODE"]);
            if ($value == $row["COURSECODE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        if ($model->coursecode == "") {
            $extra = "style=\"width:220px\" onChange=\"return btn_submit('coursecode');\"";
        } else {
            $extra = "onChange=\"return btn_submit('coursecode');\"";
        }
        $arg["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $model->coursecode, $opt, $extra, 1);
        
        //生徒
        $result = $db->query(knje061mquery::selectSchregno($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["SCHREGNO"]."：".$row["NAME_SHOW"],
                           "value" => $row["SCHREGNO"]);
        }
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHREGNO",
                            "size"       => "1",
                            "value"      => $model->schregno,
                            "extrahtml"  => "style=\"width:300px\"",
                            "options"    => $opt));
        
        $arg["SCHREGNO"] = $objForm->ge("SCHREGNO");
        
        Query::dbCheckIn($db);

        //ファイルからの取り込み
        $objForm->ae(array("type"       => "file",
                            "name"      => "FILE",
                            "size"      => 1024000,
                            "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //成績・観点データ(中学)
        //「中学調査書データ作成」チェックボックス
        $check = ($model->chugaku_chosasho == "on") ? "checked" : "";
        $extra = "id=\"CHUGAKU_CHOSASHO\" ".$check;
        $arg["CHUGAKU_CHOSASHO"] = knjCreateCheckBox($objForm, "CHUGAKU_CHOSASHO", "on", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //ラジオボタン
        $arg["RADIO"] = $model->field;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje061mindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje061mForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行 (自動生成)", $extra);

    //CSVボタン
    $extra = "onClick=\"openCsvgamen();\"";
    $arg["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);

    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    //CSVボタンのリンク先のURL
    knjCreateHidden($objForm, "URL_ATTENDREC", REQUESTROOT."/X/KNJX_E061ATTENDREC/knjx_e061attendrecindex.php");
    knjCreateHidden($objForm, "URL_STUDYREC", REQUESTROOT."/X/KNJX_E061STUDYREC/knjx_e061studyrecindex.php");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

?>
