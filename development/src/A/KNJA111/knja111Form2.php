<?php

require_once('for_php7.php');

class knja111Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("edit", "POST", "knja111index.php", "", "edit");

        $arg["reload"] = "";

        //生徒情報取得
        if (isset($model->schregno) && !isset($model->warning)) {
            $Row = knja111Query::getStudent_data($model->schregno, $model);
        }

        //DB接続
        $db = Query::dbCheckOut();

        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control["Extension"];

        //年組
        $arg["data"]["HR_NAME"] = $Row["HR_NAME"];

        //出席番号
        $arg["data"]["ATTENDNO"] = $Row["ATTENDNO"];

        //年次
        $arg["data"]["ANNUAL"] = $Row["ANNUAL"];

        //課程学科
        $arg["data"]["COURSE_MAJORNAME"] = $Row["COURSE_MAJORNAME"];

        //コース
        $arg["data"]["COURSECODENAME"] = $Row["COURSECODENAME"];

        //学籍番号
        $arg["data"]["SCHREGNO"] = $Row["SCHREGNO"];

        //内外区分
        $arg["data"]["INOUTCD"] = $db->getOne(knja111Query::getNameMst("A001", $Row["INOUTCD"]));

        //氏名
        $arg["data"]["NAME"] = $Row["NAME"];

        //表示用氏名
        $arg["data"]["NAME_SHOW"] = $Row["NAME_SHOW"];

        //氏名かな
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];

        //英字氏名
        $arg["data"]["NAME_ENG"] = $Row["NAME_ENG"];

        //戸籍氏名
        $arg["data"]["REAL_NAME"] = $Row["REAL_NAME"];

        //戸籍氏名かな
        $arg["data"]["REAL_NAME_KANA"] = $Row["REAL_NAME_KANA"];

        //誕生日
        $arg["data"]["BIRTHDAY"] = str_replace("-","/",$Row["BIRTHDAY"]);

        //性別
        $arg["data"]["SEX"] = $db->getOne(knja111Query::getNameMst("Z002", $Row["SEX"]));

        //血液型(型)
        $arg["data"]["BLOODTYPE"] = $Row["BLOODTYPE"];

        //血液型(RH型)
        $arg["data"]["BLOOD_RH"] = $Row["BLOOD_RH"];

        //その他
        $arg["data"]["HANDICAP"] = $db->getOne(knja111Query::getNameMst("A025", $Row["HANDICAP"]));

        //国籍
        $arg["data"]["NATIONALITY"] = $db->getOne(knja111Query::getNameMst("A024", $Row["NATIONALITY"]));

        //出身学校
        if ($model->schoolKind == "P") {
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = $Row["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        } else {
            //出身学校コード
            $arg["data"]["FINSCHOOLCD"] = $Row["FINSCHOOLCD"];
            //出身学校名称
            $arg["data"]["FINSCHOOL_NAME"] = $Row["FINSCHOOL_NAME"];
            //出身中学校 卒業年月日
            $arg["data"]["FINISH_DATE"] = str_replace("-","/",$Row["FINISH_DATE"]);
        }

        //再入学日付
        $query = knja111Query::getComeBackT();
        $isComeBack = $db->getOne($query) > 0 ? true : false;
        if ($isComeBack) {
            $query = knja111Query::getCB_entDate($model);
            $comeBackEntDate = $db->getOne($query);
            if ($comeBackEntDate) {
                $arg["data"]["CB_ENT_DATE"] = str_replace("-", "/", $comeBackEntDate);
            }
        }

        //入学日付
        $arg["data"]["ENT_DATE"] = str_replace("-","/",$Row["ENT_DATE"]);

        //課程入学年度
        $arg["data"]["CURRICULUM_YEAR"] = $Row["CURRICULUM_YEAR"];

        //入学区分
        $arg["data"]["ENT_DIV"] = $db->getOne(knja111Query::getNameMst("A002", $Row["ENT_DIV"]));

        //受験番号
        $arg["data"]["EXAMNO"] = $Row["EXAMNO"];

        //事由
        $arg["data"]["ENT_REASON"] = $Row["ENT_REASON"];

        //学校名
        $arg["data"]["ENT_SCHOOL"] = $Row["ENT_SCHOOL"];

        //学校住所1
        $arg["data"]["ENT_ADDR"] = $Row["ENT_ADDR"];

        //住所２使用
        if ($model->Properties["useAddrField2"] == "1") {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $arg["data"]["ENT_ADDR2"] = $Row["ENT_ADDR2"];

        //卒業日付
        $arg["data"]["GRD_DATE"] = str_replace("-","/",$Row["GRD_DATE"]);

        //卒業区分
        $arg["data"]["GRD_DIV"] = $db->getOne(knja111Query::getNameMst("A003", $Row["GRD_DIV"]));

        //転学先前日日付
        if ($model->schoolKind != "H") {
            $arg["data"]["TENGAKU_SAKI_ZENJITU"] = str_replace("-","/",$Row["TENGAKU_SAKI_ZENJITU"]);
        }

        //事由
        $arg["data"]["GRD_REASON"] = $Row["GRD_REASON"];

        //学校名
        $arg["data"]["GRD_SCHOOL"] = $Row["GRD_SCHOOL"];

        //学校住所1
        $arg["data"]["GRD_ADDR"] = $Row["GRD_ADDR"];

        //学校住所2
        $arg["data"]["GRD_ADDR2"] = $Row["GRD_ADDR2"];

        //出身塾
        $arg["data"]["PRISCHOOL_NAME"] = $Row["PRISCHOOL_NAME"];

        //備考1
        $arg["data"]["REMARK1"] = $Row["REMARK1"];

        //備考2
        $arg["data"]["REMARK2"] = $Row["REMARK2"];

        //備考3
        $arg["data"]["REMARK3"] = $Row["REMARK3"];

        //削除ボタン
        $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "subEdit"){
            $arg["reload"] = "parent.left_frame.location.href=parent.left_frame.location.href;";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja111Form2.html", $arg);
    }

}
?>
