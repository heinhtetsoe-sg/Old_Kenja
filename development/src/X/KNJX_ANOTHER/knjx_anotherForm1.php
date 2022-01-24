<?php

require_once('for_php7.php');


class knjx_anotherForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjx_anotherindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //志願者基礎データ取得
        $query = knjx_anotherQuery::getApplicantBaseData($model);

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];
        $arg["CURRICULUM_YEAR"] = $Row["CURRICULUM_YEAR"];

        //前籍校履歴データリスト取得
        $query  = knjx_anotherQuery::getList($model->schregno, $model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //リンク
            $a = array("cmd"         => "edit",
                       "APPLICANTNO" => $row["SCHREGNO"],
                       "YEAR"        => $model->year,
                       "SEQ"         => $row["SEQ"]
            );

            $row["link"] = View::alink(
                REQUESTROOT ."/X/KNJX_ANOTHER/knjx_anotherindex.php",
                htmlspecialchars($row["FINSCHOOL_NAME"]),
                "target=\"right_frame\" ",
                $a
            );
            $row["REGD_S_DATE"] = str_replace('-', '/', $row["REGD_S_DATE"]);
            $row["REGD_E_DATE"] = str_replace('-', '/', $row["REGD_E_DATE"]);

            $row["ANOTHER_SPORT"] = ($row["ANOTHER_SPORT"] == "1") ? "レ" : "";

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_anotherForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "YEAR", $model->year);

    //knjCreateHidden($objForm, "AUTH", $model->auth);
}
