<?php

require_once('for_php7.php');

class knji100c_12Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;                //対象生徒
    var $selectdata_r;                //書出し項目一覧
    var $mode;                      //生徒区分（在校生・卒業生）

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->item = array("ENTEXAM"      => "入試情報"
                            );
        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //学年
        $this->grade            = VARS::post("GRADE");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date             = VARS::post("DATE");
        //出力設定
        $this->output           = VARS::post("OUTPUT");
        //試験区分
        $this->testDiv          = VARS::post("TESTDIV");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '12';
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //学年
        if(VARS::get("GRADE"))
            $this->grade            = VARS::get("GRADE");
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output = VARS::get("OUTPUT");

        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    //CSV書出し
    function &getCsvModel() {

        if ($this->output == OUT_CODE_NAME) {
            $headerSch = array("SCHREGNO"          => "学籍番号",
                               "NAME"              => "氏名",
                               "GRADE"             => "学年",
                               "HR_CLASS"          => "組",
                               "ATTENDNO"          => "出席番号",
                               "SEXCD"             => "性別コード",
                               "SEX_NAME"          => "性別");
        } else {
            $headerSch = array("SCHREGNO"          => "学籍番号",
                               "NAME"              => "氏名",
                               "GRADE"             => "学年",
                               "HR_CLASS"          => "組",
                               "ATTENDNO"          => "出席番号",
                               "SEX"               => "性別");
        }

        $header = array("EXAMNO"            => "受験番号",
                        "NAME"              => "氏名(漢字)",
                        "NAME_KANA"         => "氏名(かな)",
                        "ABBV1"             => "性別",
                        "BIRTHDAY"          => "生年月日",
                        "ADDRESSCD"         => "現住所コード",
                        "ADDR"              => "住所",
                        "TELNO"             => "電話番号",
                        "GNAME"             => "保護者氏名(漢字)",
                        "GKANA"             => "保護者氏名(かな)",
                        "LOCATIONCD"        => "所在地コード(出身学校)",
                        "NATPUBPRIDIV"      => "国公私立区分コード",
                        "FS_CD"             => "出身学校コード",
                        "FINSCHOOL_KANA"    => "出身学校名(よみ)",
                        "FINSCHOOL_NAME"    => "出身学校名",
                        "FINSCHOOL_ADDR1"   => "出身学校住所１",
                        "FINSCHOOL_ADDR2"   => "出身学校住所２",
                        "PRISCHOOL_NAME"    => "塾名",
                        "PS_CD"             => "塾コード",
                        "NAME1"             => "出願区分",
                        "DESIREDIV"         => "出願パターン",
                        "L006ABBV"          => "専/併",
                        "SPECIAL_REASON_DIV" => "特別理由区分",
                        "EXAMCOURSE_NAME"   => "合格コース",
                        "JUDGENM"           => "合否",
                        "SCALASHIPDIV"      => "スカラシップコード",
                        "A_TOTAL"           => "入試得点(500点)",
                        "A_KOKU"            => "国語",
                        "A_SYA"             => "社会",
                        "A_SU"              => "数学",
                        "A_RIKA"            => "理科",
                        "A_ENG"             => "英語",
                        "B_TOTAL"           => "入試得点(550点)",
                        "B_KOKU"            => "国語",
                        "B_SYA"             => "社会",
                        "B_SU"              => "数学",
                        "B_RIKA"            => "理科",
                        "B_ENG"             => "英語",
                        "APPMSJ1"           => "確定コース・専願・判定1",
                        "APPMSJ2"           => "確定コース・専願・判定2",
                        "APPMSJ3"           => "確定コース・専願・判定3",
                        "APPMSJ4"           => "確定コース・専願・判定4",
                        "APPMHJ1"           => "確定コース・併願・判定1",
                        "APPMHJ2"           => "確定コース・併願・判定2",
                        "APPMHJ3"           => "確定コース・併願・判定3",
                        "APPMHJ4"           => "確定コース・併願・判定4",
                        "FS_ITEM1"          => "中学実力得点",
                        "FS_ITEM2"          => "中学実力平均",
                        "FS_ITEM4"          => "中学五ツ木",
                        "FS_ITEM5"          => "中学各種模擬",
                        "FS_COURSE_S_JUD1"  => "中学事前コース・専願・判定1",
                        "FS_COURSE_S_JUD2"  => "中学事前コース・専願・判定2",
                        "FS_COURSE_S_JUD3"  => "中学事前コース・専願・判定3",
                        "FS_COURSE_S_JUD4"  => "中学事前コース・専願・判定4",
                        "FS_COURSE_H_JUD1"  => "中学事前コース・併願・判定1",
                        "FS_COURSE_H_JUD2"  => "中学事前コース・併願・判定2",
                        "FS_COURSE_H_JUD3"  => "中学事前コース・併願・判定3",
                        "FS_COURSE_H_JUD4"  => "中学事前コース・併願・判定4",
                        "PS_ITEM1"          => "塾実力得点",
                        "PS_ITEM2"          => "塾実力平均",
                        "PS_ITEM4"          => "塾五ツ木",
                        "PS_ITEM5"          => "塾各種模擬",
                        "PS_COURSE_S_JUD1"  => "塾事前コース・専願・判定1",
                        "PS_COURSE_S_JUD2"  => "塾事前コース・専願・判定2",
                        "PS_COURSE_S_JUD3"  => "塾事前コース・専願・判定3",
                        "PS_COURSE_S_JUD4"  => "塾事前コース・専願・判定4",
                        "PS_COURSE_H_JUD1"  => "塾事前コース・併願・判定1",
                        "PS_COURSE_H_JUD2"  => "塾事前コース・併願・判定2",
                        "PS_COURSE_H_JUD3"  => "塾事前コース・併願・判定3",
                        "PS_COURSE_H_JUD4"  => "塾事前コース・併願・判定4"
                        );

        $contents  = implode($headerSch, ",");
        $contents .= ",".implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knji100c_12Query::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ設定
            $contents .= $row["SCHREGNO"].","
                        .$row["SCH_NAME"].","
                        .$row["GRADE"].","
                        .$row["HR_CLASS"].","
                        .$row["ATTENDNO"].","
                        .$row["SEXCD"].",";
            if ($this->output == OUT_CODE_NAME) {
                $contents .= $row["SEX_NAME"].",";
            }
            $contents .= $row["EXAMNO"].","
                        .$row["NAME"].","
                        .$row["NAME_KANA"].","
                        .$row["ABBV1"].","
                        .$row["BIRTHDAY"].","
                        .$row["ADDRESSCD"].","
                        .$row["ADDR"].","
                        .$row["TELNO"].","
                        .$row["GNAME"].","
                        .$row["GKANA"].","
                        .$row["LOCATIONCD"].","
                        .$row["NATPUBPRIDIV"].","
                        .$row["FS_CD"].","
                        .$row["FINSCHOOL_KANA"].","
                        .$row["FINSCHOOL_NAME"].","
                        .$row["FINSCHOOL_ADDR1"].","
                        .$row["FINSCHOOL_ADDR2"].","
                        .$row["PRISCHOOL_NAME"].","
                        .$row["PS_CD"].","
                        .$row["NAME1"].","
                        .$row["DESIREDIV"].","
                        .$row["L006ABBV"].","
                        .$row["SPECIAL_REASON_DIV"].","
                        .$row["EXAMCOURSE_NAME"].","
                        .$row["JUDGENM"].","
                        .$row["SCALASHIPDIV"].","
                        .$row["A_TOTAL"].","
                        .$row["A_KOKU"].","
                        .$row["A_SYA"].","
                        .$row["A_SU"].","
                        .$row["A_RIKA"].","
                        .$row["A_ENG"].","
                        .$row["B_TOTAL"].","
                        .$row["B_KOKU"].","
                        .$row["B_SYA"].","
                        .$row["B_SU"].","
                        .$row["B_RIKA"].","
                        .$row["B_ENG"].","
                        .$row["APPMS1"].$row["APPS1"].$row["APPJS1"].","
                        .$row["APPMS2"].$row["APPS2"].$row["APPJS2"].","
                        .$row["APPMS3"].$row["APPS3"].$row["APPJS3"].","
                        .$row["APPMS4"].$row["APPS4"].$row["APPJS4"].","
                        .$row["APPMH1"].$row["APPH1"].$row["APPJH1"].","
                        .$row["APPMH2"].$row["APPH2"].$row["APPJH2"].","
                        .$row["APPMH3"].$row["APPH3"].$row["APPJH3"].","
                        .$row["APPMH4"].$row["APPH4"].$row["APPJH4"].","
                        .$row["FS_ITEM1"].","
                        .$row["FS_ITEM2"].","
                        .$row["FS_ITEM4"].","
                        .$row["FS_ITEM5"].",";
            $contents .= $row["FS_JUDG1"].","
                        .$row["FS_JUDG2"].","
                        .$row["FS_JUDG3"].","
                        .$row["FS_JUDG4"].",";
            $contents .= $row["FH_JUDG1"].","
                        .$row["FH_JUDG2"].","
                        .$row["FH_JUDG3"].","
                        .$row["FH_JUDG4"].",";
            $contents .= $row["PS_ITEM1"].","
                        .$row["PS_ITEM2"].","
                        .$row["PS_ITEM4"].","
                        .$row["PS_ITEM5"].",";
            $contents .= $row["PS_JUDG1"].","
                        .$row["PS_JUDG2"].","
                        .$row["PS_JUDG3"].","
                        .$row["PS_JUDG4"].",";
            $contents .= $row["PH_JUDG1"].","
                        .$row["PH_JUDG2"].","
                        .$row["PH_JUDG3"].","
                        .$row["PH_JUDG4"].",";
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        $jhflg = 0;
        $row = $db->getOne(knji100c_12Query::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }

        Query::dbCheckIn($db);

        if ($jhflg == 1){
            $jhname = "中学校";
        }else {
            $jhname = "高等学校";
        }

        if ($data) {
            $filename = $this->ObjYear."年度".$jhname."入学受験者データ.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }

}
?>