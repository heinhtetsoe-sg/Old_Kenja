<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje011oForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje011oindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            $query = knje011oQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        /******************/
        /* テキストエリア */
        /******************/
        //活動内容
        $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 2, 83, "soft", "style=\"height:34px;\"", $row["TOTALSTUDYACT"]);
        //評価
        $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 3, 83, "soft", "style=\"height:48px;\"", $row["TOTALSTUDYVAL"]);
        //備考
        $arg["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 5, 83, "soft", "style=\"height:77px;\"", $row["REMARK"]);

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "COMMENTEX_A_CD",
                            "checked"   => ($row["COMMENTEX_A_CD"]==1)? true:false,
                            "value"     => 1,
                            "extrahtml" => "id=\"comment\""));
        $arg["COMMENTEX_A_CD"] = $objForm->ge("COMMENTEX_A_CD");

        /**********/
        /* ボタン */
        /**********/
        //特別な活動～ボタンを作成する
        $extra = "onclick=\"return btn_submit('form2_first');\" style=\"width:420px\"";
        $arg["btn_form2"] = knjCreateBtn($objForm, "btn_form2", "出欠の記録 ＆ 特別活動の記録 ＆ 指導上参考になる諸事項", $extra);
        //成績参照ボタンを作成する
        $extra = "onclick=\"return btn_submit('form3_first');\" style=\"width:70px\"";
        $arg["btn_form3"] = knjCreateBtn($objForm, "btn_form3", "成績参照", $extra);
        //指導要録参照画面ボタンを作成する
        $extra = "onclick=\"return btn_submit('form4_first');\" style=\"width:90px\"";
        $arg["btn_form4"] = knjCreateBtn($objForm, "btn_form4", "指導要録参照", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //セキュリティーチェック
        $securityCnt = $db->getOne(knje011oQuery::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //データCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190O/knjx190oindex.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011O&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "データ".$csvSetName, $extra);
            //ヘッダデータCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191O/knjx191oindex.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE011O&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = knjCreateBtn($objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "nextURL", $model->nextURL);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "mode", $model->mode);
        knjCreateHidden($objForm, "GRD_YEAR", $model->grd_year);
        knjCreateHidden($objForm, "GRD_SEMESTER", $model->grd_semester);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "useSyojikou3", $model->useSyojikou3);

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje011oForm1.html", $arg);
    }
}
?>
