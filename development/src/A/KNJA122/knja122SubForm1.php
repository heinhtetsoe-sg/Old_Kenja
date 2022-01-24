<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja122SubForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knja122index.php", "", "subform1");

        $arg["NAME_SHOW"] = $model->schregno."　　".$model->name;

        $db = Query::dbCheckOut();

        $year     = $model->exp_year;
        $schregno = $model->schregno;
        $query = knja122Query::getBehavior($year, $schregno);
        $result = $db->query($query);
        $checkedCode = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkedCode[] = $row["CODE"];
        }

        /********************/
        /* チェックボックス */
        /********************/
        //基本的な生活習慣
        $extra = in_array("01", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["SEIKATU"]  = knjCreateCheckBox($objForm, "SEIKATU", "01", $extra);
        //健康・体力の向上
        $extra = in_array("02", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["KENKO"]    = knjCreateCheckBox($objForm, "KENKO", "02", $extra);
        //自主・自律
        $extra = in_array("03", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["JISYU"]    = knjCreateCheckBox($objForm, "JISYU", "03", $extra);
        //責任感
        $extra = in_array("04", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["SEKININ"]  = knjCreateCheckBox($objForm, "SEKININ", "04", $extra);
        //創意工夫
        $extra = in_array("05", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["SOUI"]     = knjCreateCheckBox($objForm, "SOUI", "05", $extra);
        //思いやり・協力
        $extra = in_array("06", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["OMOIYARI"] = knjCreateCheckBox($objForm, "OMOIYARI", "06", $extra);
        //生命尊重・自然愛護
        $extra = in_array("07", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["SEIMEI"]   = knjCreateCheckBox($objForm, "SEIMEI", "07", $extra);
        //勤労・奉仕
        $extra = in_array("08", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["KINROU"]   = knjCreateCheckBox($objForm, "KINROU", "08", $extra);
        //公正・公平
        $extra = in_array("09", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["KOUSEI"]   = knjCreateCheckBox($objForm, "KOUSEI", "09", $extra);
        //公共心・公徳心
        $extra = in_array("10", $checkedCode) ? "checked=\"checked\"" : "";
        $arg["data"]["KOUKYOU"]  = knjCreateCheckBox($objForm, "KOUKYOU", "10", $extra);

        /**********/
        /* ボタン */
        /**********/
        //戻る
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻る", "onclick=\"return parent.closeit()\"");
        //更新
        $extra = "onclick=\"return btn_submit('update1');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear1');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);
        //通知票より読込
        $extra = "onclick=\"return tutihyo_yomikomi();\"";
        $arg["button"]["btn_tutihyo_yomikomi"] = knjCreateBtn($objForm, 'btn_tutihyo_yomikomi', '通知票より読込', $extra);

        /**********/
        /* hidden */
        /**********/
        $behavior_code = array('01' => 'SEIKATU',
                               '02' => 'KENKO',
                               '03' => 'JISYU',
                               '04' => 'SEKININ',
                               '05' => 'SOUI',
                               '06' => 'OMOIYARI',
                               '07' => 'SEIMEI',
                               '08' => 'KINROU',
                               '09' => 'KOUSEI',
                               '10' => 'KOUKYOU',
                               );
        $behavior_flg = false;
        for ($i = 1; $i < 11; $i++) {
            $code  = sprintf("%02d", $i);
            $query = knja122Query::getBehaviorSemesDat($code, $model);
            $count = $db->getOne($query);
            if ($count == 2) {
                $behavior_array[] = $behavior_code[$code];
                $behavior_flg = true;
            }
        }
        if ($behavior_flg) {
            $behavior_data = implode(':', $behavior_array);
        }
        knjCreateHidden($objForm, "YOMIKOMI", $behavior_data);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja122SubForm1.html", $arg);
    }
}
?>