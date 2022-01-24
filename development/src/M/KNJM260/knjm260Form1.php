<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート評価登録                                 山城 2005/03/24 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：チェック処理を追加                       山城 2005/06/29 */
/* ･NO002：チェック処理を追加                       山城 2005/07/04 */
/********************************************************************/

class knjm260Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm260index.php", "", "main");

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");
        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);

        //レポート番号
        if ($model->cmd == 'addread') {
            $model->field["REPNO"] = '';
        }
        //textbox
        if ($model->Properties["useCurriculumcd"] == "1") {
            $extra = "onkeydown=\"keyfocs1()\";";
            $arg["sel"]["REPNO"] = knjCreateTextBox($objForm, $model->field["REPNO"], "REPNO", 10, 10, $extra);
        } else if ($model->Properties["useCurriculumcd"] == "1") {
            $extra = "onkeydown=\"keyfocs1()\";";
            $arg["sel"]["REPNO"] = knjCreateTextBox($objForm, $model->field["REPNO"], "REPNO", 17, 17, $extra);
        } else {
            $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"keyfocs1()\";";
            $arg["sel"]["REPNO"] = knjCreateTextBox($objForm, $model->field["REPNO"], "REPNO", 10, 10, $extra);
        }

        //学籍番号
        if ($model->cmd == 'addread') {
            $model->field["SCHREGNO"] = '';
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\"onkeydown=\"checkkey()\";";
        $arg["sel"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 8, 8, $extra);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/
        //抽出データ出力
        $schcnt = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjm260Query::getSch($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //チェックボックス
            if($model->field["DELCHK"]["$schcnt"] == "on") {
                $check_del = "checked";
            } else {
                $check_del = "";
            }
            $extra = $check_del;
            $Row["DELCHK"] = knjCreateCheckBox($objForm, "DELCHK".$schcnt, "on", $extra);
            $Row["DELID"] = "DEL".$schcnt;

            //レポート番号
            $model->setdata["REPORTNO"][$schcnt] = substr(CTRL_YEAR,3,1).$row["SUBCLASSCD"].sprintf("%02d",$row["STANDARD_SEQ"]).$row["REPRESENT_SEQ"];

            if ("1" == $model->Properties["useCurriculumcd"]) { // 画面のレポートNo.は10桁表示
                $Row["REPORTNO_SHOW"] = substr(CTRL_YEAR,3,1).$row["SUBCLASSCD_ONLY"].sprintf("%02d",$row["STANDARD_SEQ"]).$row["REPRESENT_SEQ"];
            } else {
                $Row["REPORTNO_SHOW"] = $model->setdata["REPORTNO"][$schcnt];
            }

            $Row["REPORTNO"] = $model->setdata["REPORTNO"][$schcnt];
            $Row["REPID"] = "REP".$schcnt;

            knjCreateHidden($objForm, "REPORTNO".$schcnt, $model->setdata["REPORTNO"][$schcnt]);

            //学籍番号
            $model->setdata["SCHREGNO2"][$schcnt] = $row["SCHREGNO"];

            $Row["SCHREGNO2"] = $model->setdata["SCHREGNO2"][$schcnt];
            $Row["SCHID"] = "SCH".$schcnt;

            knjCreateHidden($objForm, "SCHREGNO2".$schcnt, $model->setdata["SCHREGNO2"][$schcnt]);

            //提出受付
            $model->setdata["RECEIPT_DATE"][$schcnt] = $row["RECEIPT_DATE"];
            //氏名（漢字）
            $model->setdata["NAME"][$schcnt] = $row["NAME_SHOW"];
            
            $Row["NAME"] = $model->setdata["NAME"][$schcnt];
            $Row["NAMEID"] = "NAME".$schcnt;

            //科目名
            $model->setdata["SUBCLASSNAME"][$schcnt] = $row["SUBCLASSNAME"];

            $Row["SUBCLASSNAME"] = $model->setdata["SUBCLASSNAME"][$schcnt];
            $Row["SCLID"] = "SCL".$schcnt;

            //回数
            $model->setdata["STANDARD_SEQ"][$schcnt] = $row["STANDARD_SEQ"];

            $Row["STANDARD_SEQ"] = "第".$model->setdata["STANDARD_SEQ"][$schcnt]."回";
            $Row["STQID"] = "RSQ".$schcnt;

            //評価
            $model->setdata["GRAD_VALUE"][$schcnt] = $row["GRAD_VALUE"];

            //再提出数
            $model->setdata["REPRESENT_SEQ"][$schcnt] = $row["REPRESENT_SEQ"];

            if ($row["REPRESENT_SEQ"] != 0) {
                $Row["REPRESENT_SEQ"] = "再".$model->setdata["REPRESENT_SEQ"][$schcnt];
                $Row["RSQID"] = "RSQ".$schcnt;
            } else {
                $Row["REPRESENT_SEQ"] = "";
                $Row["RSQID"] = "RSQ".$schcnt;
            }
            //登録日付
            $model->setdata["T_TIME"][$schcnt] = $row["RECEIPT_TIME"];

            $Row["T_TIME"] = $model->setdata["T_TIME"][$schcnt];
            $Row["TIMEID"] = "TIME".$schcnt;

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;
        $result->free();
        Query::dbCheckIn($db);
        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登　録", $extra);

        //$extra = "onclick=\"closeWin();\"";
        $extra = "onclick=\"keyThroughReSet(); closeWin();\"";	//2012/12/20 キーイベントタイムアウト処理復活
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "終　了", $extra);

        $extra = "onclick=\"return btn_submit('alldel');\"";
        $arg["button"]["all_del"] = knjCreateBtn($objForm, "all_del", "全行削除", $extra);

        $extra = "onclick=\"return btn_submit('chdel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm260Form1.html", $arg); 
    }
}
?>
