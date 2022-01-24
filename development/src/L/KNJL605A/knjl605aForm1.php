<?php

require_once('for_php7.php');

class knjl605aForm1
{
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl605aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $opt       = array();

        $result = $db->query(knjl605aQuery::selectYearQuery());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["ENTEXAMYEAR"]
                         , "value" => $row["ENTEXAMYEAR"]);
            if ($model->year == $row["ENTEXAMYEAR"]){
                $flg = true;
            }
        }
        $result->free();

        //レコードが存在しなければ処理年度を登録
        if (get_count($opt) <= 0) { 
            $opt[] = array("label" => CTRL_YEAR + 1, "value" => CTRL_YEAR + 1);
            unset($model->year);
        }
        //初期表示の年度設定
        if(!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } else {
                // 次年度以降の最小入試年度を取得
                $model->year = $db->getOne(knjl605aQuery::DeleteAtExist($model));
            }
            $reload  = "parent.right_frame.location.href='knjl605aindex.php?cmd=edit";
            $reload .= "&year=" .$model->year."';";
            $arg["reload"] = $reload;
        }

        //入試年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //テーブルの中身の作成
        $query  = knjl605aQuery::selectQuery($model->year);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $hash = array("cmd"           => "edit2"
                        , "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"]
                        , "APPLICANTDIV"  => $row["APPLICANTDIV"]
                        , "TESTDIV"       => $row["TESTDIV"]
                        , "SP_SCHOLAR_CD" => $row["SP_SCHOLAR_CD"]
                    );

            $row["SP_SCHOLAR_CD_ALINK"] = View::alink("knjl605aindex.php", $row["SP_SCHOLAR_CD"], "target=\"right_frame\"", $hash);

            $row["APPLICANTDIV_NAME"] = $row["APPLICANTDIV"] .":". $row["APPLICANTDIV_NAME"];
            $row["TESTDIV_NAME"]      = $row["TESTDIV"] .":". $row["TESTDIV_NAME"];
            $row["EXAMCOURSE"] = array();
            $query = knjl605aQuery::selectCourseQuery($row["ENTEXAMYEAR"], $row["APPLICANTDIV"], $row["TESTDIV"], $row["SP_SCHOLAR_CD"]);
            $result2 = $db->query($query);
            while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["EXAMCOURSE"][] = $row2;
            }
            $result2->free();

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $reload  = "parent.right_frame.location.href='knjl605aindex.php?cmd=edit";
            $reload .= "&year=" .$model->year."';";
            $arg["reload"] = $reload;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl605aForm1.html", $arg);
    }
}
?>
