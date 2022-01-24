<?php

require_once('for_php7.php');
class knjh150Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh150index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (isset($model->schregno) && !isset($model->warning)) {
            $Row = knjh150Query::getStudent_data($db, $model->schregno);
        } else {
            $Row =& $model->field;
        }

        makeOldRegdData($arg, $db, $model);

        //年組
        $arg["data"]["GRADE_CLASS"] = $Row["HR_NAME"];

        //番号
        $arg["data"]["ATTENDNO"] = $Row["ATTENDNO"];

        //年次
        $arg["data"]["ANNUAL"] = $Row["ANNUAL"];

        //事前処理チェック
        if (!knjh150Query::ChecktoStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //学籍番号
        $arg["data"]["SCHREGNO"] = $Row["SCHREGNO"];

        //内外区分
        $arg["data"]["INOUTCD"] = $Row["INOUTNM"];

        //課程学科
        $arg["data"]["COURSEMAJORCD"] = $Row["COURSE_SUBJECT"];

        //コース
        $arg["data"]["COURSECODE"] = $Row["COURSECODENAME"];

        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];

        //氏名
        $arg["data"]["NAME"] = $Row["NAME"];

        //表示用氏名
        $arg["data"]["NAME_SHOW"] = $Row["NAME_SHOW"];

        //氏名かな
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];

        //英字氏名
        $arg["data"]["NAME_ENG"] = $Row["NAME_ENG"];

        //誕生日
        $arg["data"]["BIRTHDAY"] = $Row["BIRTHDAY"];

        //性別
        $arg["data"]["SEX"] = $Row["SEX"];

        //血液型(型)
        $arg["data"]["BLOODTYPE"] = $Row["BLOODTYPE"];

        //血液型(RH型)
        $arg["data"]["BLOOD_RH"] = $Row["BLOOD_RH"];

        //出身中学校
        $arg["data"]["FINSCHOOL_NAME"] = $Row["FINSCHOOL_NAME"];

        //出身中学校 卒業年月日
        $arg["data"]["FINISH_DATE"] = $Row["FINISH_DATE"];

        //出身塾
        $arg["data"]["PRISCHOOL_NAME"] = $Row["PRISCHOOL_NAME"];


        //有効期間開始日付
        $arg["data"]["ISSUEDATE"] = $Row["ISSUEDATE"];
        //有効期間開始日付
        $arg["data"]["EXPIREDATE"] = $Row["EXPIREDATE"];

        //郵便番号
        $arg["data"]["ZIPCD"] = $Row["ZIPCD"];

        //地区コード
        $arg["data"]["AREACD"] = $Row["AREANAME"];

        //住所
        $arg["data"]["ADDR1"] = $Row["ADDR1"];

        //方書き(アパート名等)
        $arg["data"]["ADDR2"] = $Row["ADDR2"];

        //(英字)住所
        $arg["data"]["ADDR1_ENG"] = $Row["ADDR1_ENG"];

        //(英字)方書き(アパート名等)
        $arg["data"]["ADDR2_ENG"] = $Row["ADDR2_ENG"];

        //電話番号
        $arg["data"]["TELNO"] = $Row["TELNO"];

        //Fax番号
        $arg["data"]["FAXNO"] = $Row["FAXNO"];

        //E-mail
        $arg["data"]["EMAIL"] = $Row["EMAIL"];

        //急用連絡先
        $arg["data"]["EMERGENCYCALL"] = $Row["EMERGENCYCALL"];

        //急用氏名
        $arg["data"]["EMERGENCYNAME"] = $Row["EMERGENCYNAME"];

        //急用続柄
        $arg["data"]["EMERGENCYRELA_NAME"] = $Row["EMERGENCYRELA_NAME"];

        //急用電話番号
        $arg["data"]["EMERGENCYTELNO"] = $Row["EMERGENCYTELNO"];

        //急用連絡先2
        $arg["data"]["EMERGENCYCALL2"] = $Row["EMERGENCYCALL2"];

        //急用氏名2
        $arg["data"]["EMERGENCYNAME2"] = $Row["EMERGENCYNAME2"];

        //急用続柄2
        $arg["data"]["EMERGENCYRELA_NAME2"] = $Row["EMERGENCYRELA_NAME2"];

        //急用電話番号2
        $arg["data"]["EMERGENCYTELNO2"] = $Row["EMERGENCYTELNO2"];

        //自転車許可番号
        $arg["data"]["BICYCLE_CD"] = $Row["BICYCLE_CD"];

        //駐輪場番号
        $arg["data"]["BICYCLE_NO"] = $Row["BICYCLE_NO"];

        //前の生徒へボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "前の生徒へ",
                            "extrahtml" =>  "style=\"width:80px\" onclick=\"top.main_frame.right_frame.updateNext(self, 'pre');\""));

        //次の生徒へボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "次の生徒へ",
                            "extrahtml" =>  "style=\"width:80px\" onclick=\"top.main_frame.right_frame.updateNext(self, 'next');\""));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "_ORDER" ));

        $arg["button"]["btn_up_pre"]    = $objForm->ge("btn_up_pre");
        $arg["button"]["btn_up_next"]   = $objForm->ge("btn_up_next");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hidden
        $objForm->ae( array("type"        => "hidden",
                            "name"        => "cmd") );

        $objForm->ae( array("type"        => "hidden",
                            "name"        => "UPDATED1",
                            "value"       => $Row["UPDATED1"]) );

        $objForm->ae( array("type"        => "hidden",
                            "name"        => "UPDATED2",
                            "value"       => $Row["UPDATED2"]) );
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]    = $objForm->get_finish();

        View::toHTML($model, "knjh150Form2.html", $arg);
    }
}

function makeOldRegdData(&$arg, $db, $model) {

    $data = array("TITLE" => "", "DATA1" => "", "DATA2" => "", "DATA3" => "", "DATA4" => "", "DATA5" => "", "DATA6" => "");
    $setData = array("YEAR" => $data, "GRADE" => $data, "HR_CLASS" => $data, "ATTENDNO" => $data, "STAFF1" => $data, "STAFF2" => $data);
    $cntMax = 6;
    $cnt = 1;

    $setData["YEAR"]["TITLE"]       = "年度";
    $setData["GRADE"]["TITLE"]      = "学年-クラス";
    $setData["HR_CLASS"]["TITLE"]   = "クラス名";
    $setData["ATTENDNO"]["TITLE"]   = "番号";
    $setData["STAFF1"]["TITLE"]     = "担任";
    $setData["STAFF2"]["TITLE"]     = "副担任";

    $query = knjh150Query::getOldRegdDat($model->schregno);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($cnt > $cntMax) {
        break;
        }
        $setData["YEAR"]["DATA".$cnt] = $row["YEAR"];
        $setData["GRADE"]["DATA".$cnt] = $row["GRADE"]."-".$row["HR_CLASS"];
        $setData["HR_CLASS"]["DATA".$cnt] = $row["HR_NAME"];
        $setData["ATTENDNO"]["DATA".$cnt] = $row["ATTENDNO"];
        $setData["STAFF1"]["DATA".$cnt] = $row["STAFF1"];
        $setData["STAFF2"]["DATA".$cnt] = $row["STAFF2"];

        $cnt++;
    }
    $result->free();

    foreach ($setData as $key => $val) {
        $arg["old"][] = $val;
    }
}

?>
