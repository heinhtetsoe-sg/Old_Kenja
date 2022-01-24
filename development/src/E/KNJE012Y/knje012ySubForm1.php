<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje012ySubForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje012yindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度コンボ（通知表所見）
        $opt_year = array();
		$year_flg = false;
        $result = $db->query(knje012yQuery::getHreportremarkYear($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"],"value" => $row["YEAR"]);
            if ($model->exp_year == $row["YEAR"]) $year_flg = true;
            if ($model->year_cmb == "") $model->year_cmb = $model->exp_year;
        }
		if (!$year_flg) $model->year_cmb = $opt_year[0]["value"];

        $extra = "onChange=\"btn_submit('subform1');\"";
        $arg["YEAR_CMB"] = knjCreateCombo($objForm, "YEAR_CMB", $model->year_cmb, $opt_year, $extra, 1);

        //学期一覧取得
        $semeList = $db->getCol(knje012yQuery::getSemesterMst($model));

        //通知表所見
        $query = knje012yQuery::getHreportremarkDat($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //extra
            $extra = "style=\"height:35px;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\""; 

            //総合的な学習の時間・学習活動
            $arg["TOTALSTUDYTIME".$row["SEMESTER"]] = knjCreateTextArea($objForm, "TOTALSTUDYTIME".$row["SEMESTER"], 2, 43, "soft", $extra, $row["TOTALSTUDYTIME"]);

            //総合的な学習の時間・観点
            $arg["REMARK2".$row["SEMESTER"]] = knjCreateTextArea($objForm, "REMARK2".$row["SEMESTER"], 2, 43, "soft", $extra, $row["REMARK2"]);

            //総合的な学習の時間・評価
            $arg["REMARK1".$row["SEMESTER"]] = knjCreateTextArea($objForm, "REMARK1".$row["SEMESTER"], 2, 43, "soft", $extra, $row["REMARK1"]);

            //学期ごと
            if(in_array($row["SEMESTER"], $semeList)) {
                //出席の記録備考
                $setData1["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 2, 43, "soft", $extra, $row["ATTENDREC_REMARK"]);
                $setData1["KOUMOKU_A"] = $model->control["学期名"][$row["SEMESTER"]]."・出席の記録備考";
                //学校からの所見
                $setData2["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 2, 43, "soft", $extra, $row["COMMUNICATION"]);
                $setData2["KOUMOKU_C"] = $model->control["学期名"][$row["SEMESTER"]]."・学校からの所見";

                $arg["data1"][] = $setData1;
                $arg["data2"][] = $setData2;
            }
        }

        //観点・評価
        $result = $db->query(knje012yQuery::getHreportremarkDetailDat($model, "1"));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["REMARK03_".$row["CODE"]] = knjCreateTextArea($objForm, "REMARK03_".$row["CODE"], 2, 43, "soft", $extra, $row["REMARK1"]);
        }

        //出力項目取得
        $koumoku = array();
        $result = $db->query(knje012yQuery::getNameMst($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $koumoku[$row["NAMECD2"]] = $row["NAME1"];
        }

        //特別活動等の記録
        $dataCnt = 1;
        $result = $db->query(knje012yQuery::getHreportremarkDetailDat($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $setData3["DETAIL_REMARK1"] = knjCreateTextArea($objForm, "DETAIL_REMARK1", 2, 43, "soft", $extra, $row["REMARK1"]);
            $setData3["KOUMOKU_D"] = $koumoku[$row["CODE"]];

            if ($dataCnt == "1") {
                $setCnt = get_count($koumoku);
                $setData3["SPECIAL_SPAN"] = "<td rowspan=\"{$setCnt}\" width=\"*\" align=\"center\" nowrap>特<br>別<br>活<br>動<br>等<br>の<br>記<br>録</td>";
            } else {
                $setData3["SPECIAL_SPAN"] = "";
            }

            $arg["data3"][] = $setData3;
            $dataCnt++;
        }
        $result->free();

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje012ySubForm1.html", $arg);
    }
}
?>
