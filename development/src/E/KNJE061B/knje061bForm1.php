<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje061bForm1
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

        //学年
        $model->regdGdat = array();
        $result = $db->query(knje061bQuery::selectQueryAnnual($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if (!isset($model->annual)) {
                $model->annual = $row["VALUE"];
            }
            $model->regdGdat[$row["VALUE"]]["SCHOOL_KIND"]  = $row["SCHOOL_KIND"];
        }
        if ($model->Properties["KNJE061B_JVIEWSTAT_RECORD_DAT"] == 1) {
            $arg["kanten"] = "1";
        } else {
            $arg["kantenNo"] = "1";
        }
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SCHOOL_KIND_HIDDEN",
                            "value"     => $model->regdGdat[$model->annual]["SCHOOL_KIND"]));
        
        $objForm->ae(array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual,
                            "extrahtml"  => "style=\"width:100px\" onChange=\"return btn_submit('annual');\"",
                            "options"    => $opt));
        
        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        $result = $db->query(knje061bquery::selectQueryHRClass($model));
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["HR_NAME"],
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
        $maxdata; //コース名の最大値を格納する変数
        $volm; //コース名の最大値をbyte変換した変数
        $opt = array();
        $opt[] = (array("label" => "", "value" => ""));
        $value_flg = false;
        $result = $db->query(knje061bquery::selectCourse($model));
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
        $result = $db->query(knje061bquery::selectSchregno($model));
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

        // CSVテンプレート書出しボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_output",
                            "value"     => "テンプレート書出し",
                            "extrahtml" => "onclick=\"return btn_submit('output');\"" ));

        $arg["btn_output"] = $objForm->ge("btn_output");

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
        $arg["start"]   = $objForm->get_start("main", "POST", "knje061bindex.php", "", "main");
        
        //画面のリロード
        if ($model->cmd == "updMain") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }
        
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje061bForm1.html", $arg);
    }
}
