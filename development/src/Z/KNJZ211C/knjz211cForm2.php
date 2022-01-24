<?php

require_once('for_php7.php');

class knjz211cForm2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz211cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        //すでにあるデータの更新の場合
        if (isset($model->schoolkind) && isset($model->pattern_cd) && $model->cmd != "kakutei" && !isset($model->warning)) {
            //データ取得 -- JVIEWSTAT_LEVEL_PATTERN_YMST
            $Row = $db->getRow(knjz211cQuery::getJviewstatLevelPatternYmst($model, $model->pattern_cd), DB_FETCHMODE_ASSOC);

            //データ取得 -- JVIEWSTAT_LEVEL_PATTERN_DAT
            $query = knjz211cQuery::getJviewstatLevelPatternDat($model, $model->pattern_cd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($row as $field => $val) {
                    $Row[$field."-".$row["ASSESSLEVEL"]] = $row[$field];
                }
            }
        } else {
            $Row =& $model->field;

            if ($model->cmd == "kakutei") {
                if ($Row["ASSESSLEVEL_CNT"] > 0) {
                    for ($i = 1; $i <= $Row["ASSESSLEVEL_CNT"]; $i++) {
                        $Row["ASSESSLOW-".$i] = "";
                        $Row["ASSESSHIGH-".$i] = ($i == $Row["ASSESSLEVEL_CNT"]) ? $Row["PERFECT"] : "";
                    }
                }
            }
        }

        //校種
        if (!$model->schoolkind) {
            $sk = $db->getRow(knjz211cQuery::getSchKind($model), DB_FETCHMODE_ASSOC);
            $schoolkind = $sk["VALUE"];
            $arg["SCHOOL_KIND"] = $sk["LABEL"];
        } else {
            $schoolkind = $model->schoolkind;
            $arg["SCHOOL_KIND"] = $db->getOne(knjz211cQuery::getSchKind($model, $schoolkind));
        }

        //パターンコード
        $extra = "style=\"text-align: right\" onblur=\"checkNum(this);\"";
        $arg["PATTERN_CD"] = knjCreateTextBox($objForm, $Row["PATTERN_CD"], "PATTERN_CD", 3, 3, $extra);

        //名称
        $extra = "";
        $arg["PATTERN_NAME"] = knjCreateTextBox($objForm, $Row["PATTERN_NAME"], "PATTERN_NAME", 30, 30, $extra);

        //満点
        $extra = "style=\"text-align: right\" onblur=\"checkNum(this);\"";
        $arg["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        //段階数
        $extra = "style=\"text-align: right\" onblur=\"checkNum(this);\"";
        $arg["ASSESSLEVEL_CNT"] = knjCreateTextBox($objForm, $Row["ASSESSLEVEL_CNT"], "ASSESSLEVEL_CNT", 3, 3, $extra);

        //一覧表示
        if ($Row["ASSESSLEVEL_CNT"] > 0) {
            for ($i = 1; $i <= $Row["ASSESSLEVEL_CNT"]; $i++) {
                $setTmp = array();

                $setTmp["ASSESSLEVEL"] = $i;

                //記号
                $extra = "style=\"text-align: center\"";
                $setTmp["ASSESSMARK"] = knjCreateTextBox($objForm, $Row["ASSESSMARK-".$i], "ASSESSMARK-".$i, 4, 4, $extra);

                //下限値
                $extra = "style=\"text-align: center\" onblur=\"checkNum2(this);\"";
                $setTmp["ASSESSLOW"] = knjCreateTextBox($objForm, $Row["ASSESSLOW-".$i], "ASSESSLOW-".$i, 3, 3, $extra);

                //上限値
                $setTmp["ASSESSHIGH"] = $Row["ASSESSHIGH-".$i];
                knjCreateHidden($objForm, "ASSESSHIGH-".$i, $Row["ASSESSHIGH-".$i]);

                //表示1
                $extra = "";
                $setTmp["ASSESS_SHOW1"] = knjCreateTextBox($objForm, $Row["ASSESS_SHOW1-".$i], "ASSESS_SHOW1-".$i, 10, 20, $extra);

                //表示2
                $extra = "";
                $setTmp["ASSESS_SHOW2"] = knjCreateTextBox($objForm, $Row["ASSESS_SHOW2-".$i], "ASSESS_SHOW2-".$i, 10, 20, $extra);

                $arg["data"][] = $setTmp;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::post("cmd") == "edit" || $model->cmd == "edit2") {
            $arg["reload"] = "window.open('knjz211cindex.php?cmd=list&SCHOOL_KIND=".$schoolkind."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz211cForm2.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //確定ボタン
    $extra = "onclick=\"return btn_submit('kakutei')\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //権限
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : " disabled";

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add')\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disable);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update')\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra.$disable);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete')\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    if ($model->send_prgid) {
        //戻るボタン
        $link = REQUESTROOT."/Z/KNJZ211D/knjz211dindex.php?cmd=&PROGRAMID=KNJZ211D&GRADE=".$model->send_grade."&SUBCLASS=".$model->send_subclass;
        $extra = " onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra).'　　　　';
    } else {
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    }
}
?>
