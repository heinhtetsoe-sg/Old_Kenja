<?php

require_once('for_php7.php');

class knjd139lForm1
{
    function main(&$model)
    {
        // 表示/非表示及びテキストサイズ設定テーブル。
        // 元となったKNJD139Iから変更として、「学年の表示差異なし」、「不要な項目、設定値をを削除」

        $setdispinfo = array();
        $dispflgsetting[] = array(
                                  "moral"=>"1",
                                  "other"=>"1"
                                  );
        $setdispinfo[]    = array(
                                  "moral"=>array("data"=>array("row"=>2, "col"=>30)),
                                  "other"=>array("remark"=>array("row"=>7, "col"=>60))
                                  );

        $fieldsize = "";
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139lindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $arg["dispinputflg"] = "";
        if ($model->name != "") {
            $arg["dispinputflg"] = "1";
        } else {
            $arg["nonedispflg"] = "1";
        }

        //年次取得(年次による表示差異がなくなったので、"1"固定)
        $gradeCd = 1;

        //学期コンボ
        $query = knjd139lQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit')\"";
        $this->makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //サイズ
        $width = ($maxlen * 8 < 250) ? 250 : $maxlen * 8;
        $arg["MAIN_WIDTH"] = $width + 70;

        //道徳
        $arg["dispmoralflg"] = "";
        $dispchkmoral = $dispflgsetting[intval($gradeCd)-1]["moral"];
        if ($dispchkmoral > 0){
            //データ取得
            $rettxt = array();
            $rettxt = $db->getRow(knjd139lQuery::getMoralText($model), DB_FETCHMODE_ASSOC);

            $arg["dispmoralflg"] = "1";
            $displen = $setdispinfo[intval($gradeCd)-1]["moral"]["data"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["moral"]["data"]["row"];
            $extra = "id=\"MORAL_EVAL\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["MORAL_EVAL"] : $rettxt["TEXT01"];
            $arg["text"]["moral_eval"] = knjCreateTextArea($objForm, "MORAL_EVAL", $disprow, $displen, "", $extra, $tmpsettxt);
            $arg["text"]["moral_eval_comment"] = '(全角で ' . $displen . '文字X' . $disprow .'行)';
            $fieldsize .= "MORAL_DETAIL=".$disprow."=".$displen."=道徳(評価),";
            knjCreateHidden($objForm, "MORAL_EVAL_KETA", $displen);
            knjCreateHidden($objForm, "MORAL_EVAL_GYO", $disprow);
            KnjCreateHidden($objForm, "MORAL_EVAL_STAT", "statusarea4");
        }

        //出欠・特記事項
        $arg["dispremarkflg"] = "";
        $dispchkactclb = $dispflgsetting[intval($gradeCd)-1]["other"];
        if ($dispchkactclb > 0){
            //データ取得
            $rettxt = array();
            $rettxt = $db->getRow(knjd139lQuery::getRemarkText($model), DB_FETCHMODE_ASSOC);

            $arg["dispremarkflg"] = "1";

            $displen = $setdispinfo[intval($gradeCd)-1]["other"]["remark"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["other"]["remark"]["row"];
            $extra = "id=\"REMARK_TEACHERCOMMENT\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["REMARK_TEACHERCOMMENT"] : $rettxt["TEXT02"];
            $arg["text"]["remark_teachercomment"] = knjCreateTextArea($objForm, "REMARK_TEACHERCOMMENT", $disprow, $displen, "", $extra, $tmpsettxt);
            $arg["text"]["remark_teachercomment_comment"] = '(全角で ' . $displen . '文字X' . $disprow .'行)';
            $fieldsize .= "REMARK_TCDETAIL=".$disprow."=".$displen."="."特記事項・担任からの通信2,";
            knjCreateHidden($objForm, "REMARK_TEACHERCOMMENT_KETA", $displen);
            knjCreateHidden($objForm, "REMARK_TEACHERCOMMENT_GYO", $disprow);
            KnjCreateHidden($objForm, "REMARK_TEACHERCOMMENT_STAT", "statusarea9");
        }

        //学校生活の様子
        $arg["disptotalstdyflg"] = "1";
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_LM/knjd_behavior_lmindex.php?CALL_PRG="."KNJD139L"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHOOL_KIND=P&SCHREGNO=".$model->schregno."&GRADE=".$model->grade."&send_knjdBehaviorsd_UseText_P=".$model->Properties["knjdBehaviorsd_UseText_P"]."',0,0,800,500);\"";
        $arg["button"]["btn_schoollifechk"] = KnjCreateBtn($objForm, "btn_schoollifechk", "学校生活の様子", $extra);

        //更新ボタン
        $extra = "id=\"btn_update\" onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へ/更新後次の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);
        if (get_count($model->warning) == 0 && $model->cmd != "clean") {
            $arg["NOT_WARNING"] = 1;
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clean") {
            $arg["NOT_WARNING"] = 1;
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if (get_count($model->warning) == 0 && $model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd139lForm1.html", $arg);

    }

    //コンボ作成
    function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
        $opt = array();
        if ($blank != "") $opt[] = array("label" => "", "value" => "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }

        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

        $result->free();
    }
}
?>
