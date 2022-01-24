<?php

require_once('for_php7.php');

class knji130Form2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("edit", "POST", "knji130index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度、学期表示
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        //復学対象生徒判定
        $fukugaku = ($db->getOne(knji130Query::checkSchData($model))) ? false : true;
        knjCreateHidden($objForm, "FUKUGAKU", $fukugaku);

        if (!$fukugaku) {
            $model->exp_year     = CTRL_YEAR;
            $model->exp_semester = CTRL_SEMESTER;
        }

        //生徒データ取得
        if (isset($model->schregno)) {
            $query = knji130Query::getStudentData($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //対象項目にデータをセット
        if (isset($model->schregno) && !isset($model->warning)) {
            $row = ($fukugaku) ? array() : $Row;
        } else {
            $row =& $model->field;
        }

        //年組コンボボックス
        $query = knji130Query::getGradeHrClass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRCL", $row["GRCL"], $extra, 1);

        //出席番号
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ATTENDNO"] = knjCreateTextBox($objForm, $row["ATTENDNO"], "ATTENDNO", 3, 3, $extra);

        //年次
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ANNUAL"] = knjCreateTextBox($objForm, $row["ANNUAL"], "ANNUAL", 2, 2, $extra);

        //課程学科
        $query = knji130Query::getCourseMajor();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COURSEMAJORCD", $row["COURSEMAJORCD"], $extra, 1);

        //コース
        $query = knji130Query::getCourseCode();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $row["COURSECODE"], $extra, 1);

        //学籍番号
        $arg["data"]["SCHREGNO"] = $Row["SCHREGNO"];

        //内外区分
        $query = knji130Query::getNameMst("A001", "", 4);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INOUTCD", $row["INOUTCD"], $extra, 1);

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
        $arg["data"]["BIRTHDAY"] = str_replace("-", "/", $Row["BIRTHDAY"]);

        //性別
        $arg["data"]["SEX"] = ($Row["SEX"]) ? $db->getOne(knji130Query::getNameMst("Z002", $Row["SEX"], 1)) : "";

        //血液型(型)
        $arg["data"]["BLOODTYPE"] = $Row["BLOODTYPE"];

        //血液型(RH型)
        $arg["data"]["BLOOD_RH"] = $Row["BLOOD_RH"];

        //その他
        $arg["data"]["HANDICAP"] = ($Row["HANDICAP"]) ? $db->getOne(knji130Query::getNameMst("A025", $Row["HANDICAP"], 1)) : "";

        //国籍
        $arg["data"]["NATIONALITY"] = ($Row["NATIONALITY"]) ? $db->getOne(knji130Query::getNameMst("A024", $Row["NATIONALITY"], 1)) : "";

        //出身中学校
        if ($model->Properties["use_finSchool_teNyuryoku_{$model->schoolKind}"] == "1") {
            $arg["NYUGAKUMAE_SYUSSIN_JOUHOU"] = 1;
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = $Row["NYUGAKUMAE_SYUSSIN_JOUHOU"];
        } else {
            $arg["FINSCHOOLCD"] = 1;
            $arg["data"]["FINSCHOOLCD"] = $Row["FINSCHOOLCD"];
            $finschoolname = $db->getOne(knji130Query::getFinschoolName($Row["FINSCHOOLCD"]));
            $arg["data"]["FINSCHOOLNAME"] = $finschoolname;
            $arg["data"]["FINSCHOOL_SEP"] = ($Row["FINSCHOOLCD"] && $finschoolname) ? "：" : "";

            //出身中学校 卒業年月日
            $arg["data"]["FINISH_DATE"] = str_replace("-", "/", $Row["FINISH_DATE"]);
        }

        //入学日付
        $arg["data"]["ENT_DATE"] = str_replace("-", "/", $Row["ENT_DATE"]);

        //課程入学年度
        $arg["data"]["CURRICULUM_YEAR"] = $Row["CURRICULUM_YEAR"];

        //入学区分
        $arg["data"]["ENT_DIV"] = $db->getOne(knji130Query::getNameMst("A002", $Row["ENT_DIV"], 1));

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

        //卒業区分
        $arg["data"]["GRD_DIV"] = ($Row["GRD_DIV"]) ? $db->getOne(knji130Query::getNameMst("A003", $Row["GRD_DIV"], 1)) : "";

        //自校退校日
        $arg["data"]["GRD_DATE"] = str_replace("-", "/", $Row["GRD_DATE"]);

        //転学先前日
        $arg["data"]["TENGAKU_SAKI_ZENJITU"] = str_replace("-", "/", $Row["TENGAKU_SAKI_ZENJITU"]);

        //転学先学年
        $arg["data"]["TENGAKU_SAKI_GRADE"] = $Row["TENGAKU_SAKI_GRADE"];

        //事由
        $arg["data"]["GRD_REASON"] = $Row["GRD_REASON"];

        //学校名
        $arg["data"]["GRD_SCHOOL"] = $Row["GRD_SCHOOL"];

        //学校住所1
        $arg["data"]["GRD_ADDR"] = $Row["GRD_ADDR"];

        //学校住所2
        $arg["data"]["GRD_ADDR2"] = $Row["GRD_ADDR2"];

        //学校住所2（項目名）
        $arg["data"]["GRD_ADDR2_LABEL"] = ($model->schoolKind == "H") ? '課程・学科等' : '学校住所2';

        //復学用入力項目
        if ($model->cmd != "editFuku" && $fukugaku) {
            $row["ENT_DATE"] = "";
            $row["ENT_DIV"] = "";
            $row["ENT_REASON"] = "";
            $row["ENT_SCHOOL"] = "";
            $row["CURRICULUM_YEAR"] = "";
            $row["ENT_ADDR"] = "";
            $row["ENT_ADDR2"] = "";
        }

        //入学
        $arg["dataIn"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE", str_replace("-", "/", $row["ENT_DATE"]), "");

        //課程入学年度
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["dataIn"]["CURRICULUM_YEAR"] = knjCreateTextBox($objForm, $row["CURRICULUM_YEAR"], "CURRICULUM_YEAR", 4, 4, $extra);

        //入学区分
        $arg["dataIn"]["ENT_DIV"] = $model->createCombo($objForm, $db, "A002", "ENT_DIV", $row["ENT_DIV"], 1);

        //事由
        $extra = "";
        $arg["dataIn"]["ENT_REASON"] = knjCreateTextBox($objForm, $row["ENT_REASON"], "ENT_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["dataIn"]["ENT_SCHOOL"] = knjCreateTextBox($objForm, $row["ENT_SCHOOL"], "ENT_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["dataIn"]["ENT_ADDR"] = knjCreateTextBox($objForm, $row["ENT_ADDR"], "ENT_ADDR", 45, 90, $extra);

        //住所２使用
        if ($model->Properties["useAddrField2"] == "1") {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $extra = "";
        $arg["dataIn"]["ENT_ADDR2"] = knjCreateTextBox($objForm, $row["ENT_ADDR2"], "ENT_ADDR2", 45, 90, $extra);

        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control["Extension"];
        if ($model->Properties["useDispUnDispPicture"] === '1') {
            $arg["unDispPicture"] = "1";
        } else {
            $arg["dispPicture"] = "1";
        }

        //復学ボタン
        $extra = "onclick=\"return btn_submit('fukugaku');\"";
        $name  = ($fukugaku) ? "復 学" : "復学済";
        $arg["button"]["btn_fukugaku"] = knjCreateBtn($objForm, "btn_fukugaku", $name, $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED1", $Row["UPDATED1"]);
        knjCreateHidden($objForm, "UPDATED2", $Row["UPDATED2"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knji130Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
