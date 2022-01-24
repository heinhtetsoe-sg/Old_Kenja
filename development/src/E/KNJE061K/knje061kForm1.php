<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje061kForm1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();
        $objForm = new form();

        //年度学期表示
        $arg["SEMESTERNAME"] = CTRL_YEAR ."年度　" .CTRL_SEMESTERNAME;

        //プロパティKNJE061_JVIEWSTAT_RECORD_DAT=1の時、「○観点データ」を表示する
        if ($model->Properties["KNJE061_JVIEWSTAT_RECORD_DAT"] == 1) {
            $arg["kanten"] = "1";
        } else {
            $arg["kantenNo"] = "1";
        }

        //学年
        $result = $db->query(knje061kQuery::selectQueryAnnual($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => (int)$row["ANNUAL"] ."学年",
                           "value" => $row["ANNUAL"]);
            if (!isset($model->annual)) {
                $model->annual = $row["ANNUAL"];
            }
        }

        $objForm->ae(array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('annual');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        $result = $db->query(knje061kquery::selectQueryHRClass($model->annual));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["HR_CLASS"]."組",
                           "value" => $row["HR_CLASS"]);
        }

        $objForm->ae(array("type"       => "select",
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
        $result = $db->query(knje061kquery::selectCourse($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECODE"]."：".$row["COURSECODENAME"],
                           "value" => $row["COURSECODE"]);
            if ($value == $row["COURSECODE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        if ($model->coursecode == "") {
            $extra = "style=\"width:220px\" onChange=\"return btn_submit('coursecode');\"";
        } else {
            $extra = "onChange=\"return btn_submit('coursecode');\"";
        }
        $arg["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $model->coursecode, $opt, $extra, 1);

        //生徒
        $result = $db->query(knje061kquery::selectSchregno($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SCHREGNO"]."：".$row["NAME_SHOW"],
                           "value" => $row["SCHREGNO"]);
        }

        $objForm->ae(array("type"       => "select",
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

        //CSV取込みボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //ラジオボタン
        $arg["RADIO"] = $model->field;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje061kindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje061kForm1.html", $arg);
    }
}
