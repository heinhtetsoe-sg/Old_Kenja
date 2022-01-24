<?php

require_once('for_php7.php');

class knjm240mForm1
{
    public function main(&$model)
    {

        //セキュリティーチェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjm240mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックス
        $result = $db->query(knjm240mQuery::getSubClasyearQuery());
        $opt_year = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"],
                                  "value" => $row["YEAR"]);
        }
        if (!isset($model->Year)) {
            $model->Year = CTRL_YEAR;
        }
        $extra = "onChange=\"btn_submit('init');\" ";
        $arg["GrYEAR"] = knjCreateCombo($objForm, "GrYEAR", $model->Year, $opt_year, $extra, 1);

        //前年度コピーボタン
        $extra = " onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //科目一覧
        $result = $db->query(knjm240mQuery::readQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $row["SUBCLASS_SHOW"] = View::alink(
                "knjm240mindex.php",
                $row["SUBCLASSNAME"],
                "target=\"right_frame\"",
                array("cmd"             => "edit",
                                                      "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                                      "CHAIRCD"         => $row["CHAIRCD"].$row["SUBCLASSCD"],
                                                      "SUBCLASS_SHOW"   => $row["SUBCLASSNAME"],
                                                      "GetYear"         => $model->Year,
                                                )
            );
            $row["SUBCNT"] = $row["REP_SEQ_ALL"];
            $row["SUBCHECK"] = $row["REP_LIMIT"];
            $row["SUBSTART"] = $row["REP_START_SEQ"];
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "init") {
            $path = REQUESTROOT ."/M/KNJM240M/knjm240mindex.php?cmd=edit&GetYear=".$model->Year;
            $arg["reload"] = "window.open('$path','right_frame');";
        }

        View::toHTML($model, "knjm240mForm1.html", $arg);
    }
}
