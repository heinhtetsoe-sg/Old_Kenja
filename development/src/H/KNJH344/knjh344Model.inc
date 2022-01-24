<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh344Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $z010Name1;
        
    /**
     * メンバ変数の初期化
     */
    function knjh344Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = knjh344Query::getZ010Name1();
        $this->z010Name1 = $db->getOne($query);
        Query::dbCheckIn($db);
        $this->field = array(
                             "YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SCHKIND"              =>  VARS::post("SCHKIND"),              //校種
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "SELSUBJECT_NEXTYEAR"  =>  VARS::post("SELSUBJECT_NEXTYEAR"),  //次年度選択科目
                             "RATINGAVE"            =>  VARS::post("RATINGAVE"),            //評定平均
                             "AMBITIONSCHOOL"       =>  VARS::post("AMBITIONSCHOOL"),       //志望校
                             "OUTSIDE_TRIALTEST1"   =>  VARS::post("OUTSIDE_TRIALTEST1"),   //校外模試1チェックボックス
                             "OUTSIDE_TRIALTEST2"   =>  VARS::post("OUTSIDE_TRIALTEST2"),   //校外模試2チェックボックス
                             "TRIALTEST1_NAME"      =>  VARS::post("TRIALTEST1_NAME"),      //模試選択1
                             "TRIALTEST2_NAME"      =>  VARS::post("TRIALTEST2_NAME")       //模試選択2
                            );

        $this->cmd         = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjh344Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }

    //CSV出力処理
    function getCsvModel() {
        if ($this->z010Name1 == 'sundaikoufu') {
            $schregnotext = "学籍番号";
            $schregnotext4 = "学籍番号";
            $namekanatext4 = "読み";
            $attendnotext = "出席番号";
            $finschoolnametext = SCHOOLKIND == "P" ? "出身園" : "出身学校";
        } else {
            $schregnotext = $this->sch_label."手帳番号";
            $schregnotext4 = $this->sch_label."番号";
            $namekanatext4 = "よみ";
            $attendnotext = "学級番号";
            $finschoolnametext = "出身学校";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $this->hopeCnt = array();
        $this->scoreCnt = array();
        if ($this->field["OUTSIDE_TRIALTEST1"] == 1) {
            //希望フィールド数
            $query = knjh344Query::getHopeField($this, $this->field["TRIALTEST1_NAME"]);
            $this->hopeCnt[0] = $db->getOne($query);
            //得点フィールド数
            $query = knjh344Query::getScoreField($this, $this->field["TRIALTEST1_NAME"]);
            $this->scoreCnt[0] = $db->getOne($query);
        } else {
            $this->hopeCnt[0] = 0;
            $this->scoreCnt[0] = 0;
        }
//var_dump($this->hopeCnt[0]);
//var_dump($this->scoreCnt[0]);
        if ($this->field["OUTSIDE_TRIALTEST2"] == 1) {
            //希望フィールド数
            $query = knjh344Query::getHopeField($this, $this->field["TRIALTEST2_NAME"]);
            $this->hopeCnt[1] = $db->getOne($query);
            //得点フィールド数
            $query = knjh344Query::getScoreField($this, $this->field["TRIALTEST2_NAME"]);
            $this->scoreCnt[1] = $db->getOne($query);
        } else {
            $this->hopeCnt[1] = 0;
            $this->scoreCnt[1] = 0;
        }
//var_dump($this->hopeCnt[1]);
//var_dump($this->scoreCnt[1]);

        //ヘッダのグループ名
        $query = knjh344Query::getHeadGrpSbjName($this);
        $result = $db->query($query);
        $grpSbjName = array();
        while ($wkrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grpSbjName[] = $wkrow["ABBV"];
        }

        //DB切断
        Query::dbCheckIn($db);

        //ヘッダ項目（出力対象項目）
        $header = array();
        $headeridx = array();
        $contents  = "";
//        if ($this->z010Name1 == 'bunkyo') {
//            $header[1] = array( "HR_NAME"           => "年組",
//                                "STAFFNAME"         => "担任名",
//                                "SCHREGNO"          => $schregnotext,
//                                "ATTENDNO"          => $attendnotext,
//                                "NAME"              => "氏名",
//                                "NAME_KANA"         => "ふりがな",
//                                "BIRTHDAY"          => "生年月日",
//                                "ZIPCD"             => "郵便番号",
//                                "ADDR1"             => "現住所",
//                                "ADDR2"             => "現住所２",
//                                "TEL"               => "電話番号",
//                                "GUARD_NAME"        => "保護者氏名",
//                                "E_TEL"             => "急用電話番号"
//                              );
//
//            $header[2] = array( "HR_NAME"           => "年組",
//                                "STAFFNAME"         => "担任名",
//                                "SCHREGNO"          => $schregnotext,
//                                "ATTENDNO"          => $attendnotext,
//                                "NAME"              => "氏名",
//                                "NAME_KANA"         => "ふりがな",
//                                "BIRTHDAY"          => "生年月日",
//                                "SEX"               => "性別",
//                                "ZIPCD"             => "郵便番号",
//                                "ADDR1"             => "現住所",
//                                "ADDR2"             => "現住所２",
//                                "TEL"               => "電話番号",
//                                "GUARD_NAME"        => "保護者氏名",
//                                "E_TEL"             => "急用電話番号",
//                                "REMARK"            => "備考"
//                              );
//        } else {
              $header[1] = array(
                                "YEAR"            => "年度",
                                "SEMESTER"        => "学期",
                                "COURSENAME"      => "過程",
                                "MAJORNAME"       => "学科",
                                "COURSECODENAME"  => "コース",
                                "GRADENAME"       => "学年",
                                "HR_CLASSNAME"    => "組",
                                "ATTENDNO"        => $attendnotext,
                                "SCHREGNO"        => $schregnotext,
                                "NAME"            => "氏名"
                              );
              $headeridx[] = 1;
              $contents .= implode($header[1], ",");
              $header[2] = array(
                                "SBJ_A"      => "a群 科目名",      //★
                                "SBJ_B"      => "b群 科目名",      //★
                                "SBJ_C"      => "c群 科目名",      //★
                                "SBJ_D"      => "d群 科目名",      //★
                                "SBJ_E"      => "e群 科目名",      //★
                                "SBJ_F"      => "f群 科目名",      //★
                                "SBJ_G"      => "g群 科目名",      //★
                                "SBJ_H"      => "h群 科目名",      //★
                                "SBJ_I"     => "必修理科 科目名", //★
                                "SBJ_J"     => "必修地歴 科目名", //★
                                "SBJ_K"     => "AB群 科目名",     //★
                                "SBJ_L"     => "CD群 科目名"      //★
                              );
              $wkidx = 0;
              foreach ($header[2] as $key => $val) {
                  if (get_count($grpSbjName) > $wkidx) {
                      $header[2][$key] = $grpSbjName[$wkidx];
                  }
                  $wkidx++;
              }
              if ($this->field["SELSUBJECT_NEXTYEAR"] == 1) {
                  $headeridx[] = 2;
                  $contents .= ",".implode($header[2], ",");
              }
              $header[3] = array(
                                "SBJNAME_1"      => "教科名",     //★教科名
                                "SBJ_AVG1"       => "評定平均",   //★
                                "SBJNAME_2"      => "教科名",     //★教科名
                                "SBJ_AVG2"       => "評定平均",   //★
                                "SBJNAME_3"      => "教科名",     //★教科名
                                "SBJ_AVG3"       => "評定平均",   //★
                                "SBJNAME_4"      => "教科名",     //★教科名
                                "SBJ_AVG4"       => "評定平均",   //★
                                "SBJNAME_5"      => "教科名",     //★教科名
                                "SBJ_AVG5"       => "評定平均",   //★
                                "SBJNAME_6"      => "教科名",     //★教科名
                                "SBJ_AVG6"       => "評定平均",   //★
                                "SBJNAME_7"      => "教科名",     //★教科名
                                "SBJ_AVG7"       => "評定平均",   //★
                                "SBJNAME_8"      => "教科名",     //★教科名
                                "SBJ_AVG8"       => "評定平均",   //★
                                "SBJNAME_9"      => "教科名",     //★教科名
                                "SBJ_AVG9"       => "評定平均"    //★
                              );
              if ($this->field["RATINGAVE"] == 1) {
                  $headeridx[] = 3;
                  $contents .= ",".implode($header[3], ",");
              }
              $header[4] = array(
                                "MOSI1NAME"             => "模試1名称",
                                "HOPESCHNAME1_1"           => "模試1志望大学1情報",
                                "HOPEJUDGE1_1"            => "模試1志望大学1判定",
                                "HOPESCHNAME1_2"           => "模試1志望大学2情報",
                                "HOPEJUDGE1_2"            => "模試1志望大学2判定",
                                "HOPESCHNAME1_3"           => "模試1志望大学3情報",
                                "HOPEJUDGE1_3"            => "模試1志望大学3判定",
                                "HOPESCHNAME1_4"           => "模試1志望大学4情報",
                                "HOPEJUDGE1_4"            => "模試1志望大学4判定",
                                "HOPESCHNAME1_5"           => "模試1志望大学5情報",
                                "HOPEJUDGE1_5"            => "模試1志望大学5判定"
                                );
              if ($this->field["AMBITIONSCHOOL"] == 1 && $this->field["OUTSIDE_TRIALTEST1"] == 1) {
                  $headeridx[] = 4;
                  $contents .= ",".implode($header[4], ",");
              }
              $header[5] = array(
                                "TDEVIATION1_01"         => "模試1科目1名称",
                                "DEVIATION1_01"          => "模試1科目1偏差値",
                                "TDEVIATION1_02"         => "模試1科目2名称",
                                "DEVIATION1_02"          => "模試1科目2偏差値",
                                "TDEVIATION1_03"         => "模試1科目3名称",
                                "DEVIATION1_03"          => "模試1科目3偏差値",
                                "TDEVIATION1_04"         => "模試1科目4名称",
                                "DEVIATION1_04"          => "模試1科目4偏差値",
                                "TDEVIATION1_05"         => "模試1科目5名称",
                                "DEVIATION1_05"          => "模試1科目5偏差値",
                                "TDEVIATION1_06"         => "模試1科目6名称",
                                "DEVIATION1_06"          => "模試1科目6偏差値",
                                "TDEVIATION1_07"         => "模試1科目7名称",
                                "DEVIATION1_07"          => "模試1科目7偏差値",
                                "TDEVIATION1_08"         => "模試1科目8名称",
                                "DEVIATION1_08"          => "模試1科目8偏差値",
                                "TDEVIATION1_09"         => "模試1科目9名称",
                                "DEVIATION1_09"          => "模試1科目9偏差値",
                                "TDEVIATION1_10"         => "模試1科目10名称",
                                "DEVIATION1_10"          => "模試1科目10偏差値",
                                "TDEVIATION1_11"         => "模試1科目11名称",
                                "DEVIATION1_11"          => "模試1科目11偏差値",
                                "TDEVIATION1_12"         => "模試1科目12名称",
                                "DEVIATION1_12"          => "模試1科目12偏差値",
                                "TDEVIATION1_13"         => "模試1科目13名称",
                                "DEVIATION1_13"          => "模試1科目13偏差値",
                                "TDEVIATION1_14"         => "模試1科目14名称",
                                "DEVIATION1_14"          => "模試1科目14偏差値",
                                "TDEVIATION1_15"         => "模試1科目15名称",
                                "DEVIATION1_15"          => "模試1科目15偏差値",
                                "TDEVIATION1_16"         => "模試1科目16名称",
                                "DEVIATION1_16"          => "模試1科目16偏差値",
                                "TDEVIATION1_17"         => "模試1科目17名称",
                                "DEVIATION1_17"          => "模試1科目17偏差値",
                                "TDEVIATION1_18"         => "模試1科目18名称",
                                "DEVIATION1_18"          => "模試1科目18偏差値",
                                "TDEVIATION1_19"         => "模試1科目19名称",
                                "DEVIATION1_19"          => "模試1科目19偏差値",
                                "TDEVIATION1_20"         => "模試1科目20名称",
                                "DEVIATION1_20"          => "模試1科目20偏差値",
                                "TDEVIATION1_21"         => "模試1科目21名称",
                                "DEVIATION1_21"          => "模試1科目21偏差値",
                                "TDEVIATION1_22"         => "模試1科目22名称",
                                "DEVIATION1_22"          => "模試1科目22偏差値",
                                "TDEVIATION1_23"         => "模試1科目23名称",
                                "DEVIATION1_23"          => "模試1科目23偏差値",
                                "TDEVIATION1_24"         => "模試1科目24名称",
                                "DEVIATION1_24"          => "模試1科目24偏差値",
                                "TDEVIATION1_25"         => "模試1科目25名称",
                                "DEVIATION1_25"          => "模試1科目25偏差値",
                                "TDEVIATION1_26"         => "模試1科目26名称",
                                "DEVIATION1_26"          => "模試1科目26偏差値"
                              );
              if ($this->field["OUTSIDE_TRIALTEST1"] == 1) {
                  $headeridx[] = 5;
                  $contents .= ",".implode($header[5], ",");
              }
              $header[6] = array(
                                "MOSI2NAME"             => "模試2名称",
                                "HOPESCHNAME2_1"           => "模試2志望大学1情報",
                                "HOPEJUDGE2_1"            => "模試2志望大学1判定",
                                "HOPESCHNAME2_2"           => "模試2志望大学2情報",
                                "HOPEJUDGE2_2"            => "模試2志望大学2判定",
                                "HOPESCHNAME2_3"           => "模試2志望大学3情報",
                                "HOPEJUDGE2_3"            => "模試2志望大学3判定",
                                "HOPESCHNAME2_4"           => "模試2志望大学4情報",
                                "HOPEJUDGE2_4"            => "模試2志望大学4判定",
                                "HOPESCHNAME2_5"           => "模試2志望大学5情報",
                                "HOPEJUDGE2_5"            => "模試2志望大学5判定"
                                );
              if ($this->field["AMBITIONSCHOOL"] == 1 && $this->field["OUTSIDE_TRIALTEST2"] == 1) {
                  $headeridx[] = 6;
                  $contents .= ",".implode($header[6], ",");
              }
                                
              $header[7] = array(
                                "TDEVIATION2_01"          => "模試2科目1名称",
                                "DEVIATION2_01"           => "模試2科目1偏差値",
                                "TDEVIATION2_02"          => "模試2科目2名称",
                                "DEVIATION2_02"           => "模試2科目2偏差値",
                                "TDEVIATION2_03"          => "模試2科目3名称",
                                "DEVIATION2_03"           => "模試2科目3偏差値",
                                "TDEVIATION2_04"          => "模試2科目4名称",
                                "DEVIATION2_04"           => "模試2科目4偏差値",
                                "TDEVIATION2_05"          => "模試2科目5名称",
                                "DEVIATION2_05"           => "模試2科目5偏差値",
                                "TDEVIATION2_06"          => "模試2科目6名称",
                                "DEVIATION2_06"           => "模試2科目6偏差値",
                                "TDEVIATION2_07"          => "模試2科目7名称",
                                "DEVIATION2_07"           => "模試2科目7偏差値",
                                "TDEVIATION2_08"          => "模試2科目8名称",
                                "DEVIATION2_08"           => "模試2科目8偏差値",
                                "TDEVIATION2_09"          => "模試2科目9名称",
                                "DEVIATION2_09"           => "模試2科目9偏差値",
                                "TDEVIATION2_10"          => "模試2科目10名称",
                                "DEVIATION2_10"           => "模試2科目10偏差値",
                                "TDEVIATION2_11"          => "模試2科目11名称",
                                "DEVIATION2_11"           => "模試2科目11偏差値",
                                "TDEVIATION2_12"          => "模試2科目12名称",
                                "DEVIATION2_12"           => "模試2科目12偏差値",
                                "TDEVIATION2_13"          => "模試2科目13名称",
                                "DEVIATION2_13"           => "模試2科目13偏差値",
                                "TDEVIATION2_14"          => "模試2科目14名称",
                                "DEVIATION2_14"           => "模試2科目14偏差値",
                                "TDEVIATION2_15"          => "模試2科目15名称",
                                "DEVIATION2_15"           => "模試2科目15偏差値",
                                "TDEVIATION2_16"          => "模試2科目16名称",
                                "DEVIATION2_16"           => "模試2科目16偏差値",
                                "TDEVIATION2_17"          => "模試2科目17名称",
                                "DEVIATION2_17"           => "模試2科目17偏差値",
                                "TDEVIATION2_18"          => "模試2科目18名称",
                                "DEVIATION2_18"           => "模試2科目18偏差値",
                                "TDEVIATION2_19"          => "模試2科目19名称",
                                "DEVIATION2_19"           => "模試2科目19偏差値",
                                "TDEVIATION2_20"          => "模試2科目20名称",
                                "DEVIATION2_20"           => "模試2科目20偏差値",
                                "TDEVIATION2_21"          => "模試2科目21名称",
                                "DEVIATION2_21"           => "模試2科目21偏差値",
                                "TDEVIATION2_22"          => "模試2科目22名称",
                                "DEVIATION2_22"           => "模試2科目22偏差値",
                                "TDEVIATION2_23"          => "模試2科目23名称",
                                "DEVIATION2_23"           => "模試2科目23偏差値",
                                "TDEVIATION2_24"          => "模試2科目24名称",
                                "DEVIATION2_24"           => "模試2科目24偏差値",
                                "TDEVIATION2_25"          => "模試2科目25名称",
                                "DEVIATION2_25"           => "模試2科目25偏差値",
                                "TDEVIATION2_26"          => "模試2科目26名称",
                                "DEVIATION2_26"           => "模試2科目26偏差値"
                              );
              if ($this->field["OUTSIDE_TRIALTEST2"] == 1) {
                  $headeridx[] = 7;
                  $contents .= ",".implode($header[7], ",");
              }
              $contents .= "\n";

//            $header[2] = array( "HR_NAME"           => "年組",
//                                "STAFFNAME"         => "担任名",
//                                "SCHREGNO"          => $schregnotext,
//                                "ATTENDNO"          => $attendnotext,
//                                "NAME"              => "氏名",
//                                "NAME_KANA"         => "ふりがな",
//                                "BIRTHDAY"          => "生年月日",
//                                "SEX"               => "性別",
//                                "ZIPCD"             => "郵便番号",
//                                "ADDR1"             => "現住所",
//                                "TEL"               => "電話番号",
//                                "GUARD_NAME"        => "保護者氏名",
//                                "E_TEL"             => "急用電話番号",
//                                "REMARK"            => "備考"
//                              );
//        }

//        $header[3]["HR_NAME"] = "年組";
//        $header[3]["STAFFNAME"] = "担任名";
//        $header[3]["ATTENDNO"] = "出席番号";
//        $header[3]["NAME"] = "氏名";
//        $header[3]["SEX"] = "性別";
//        $header[3]["GUARD_NAME"] = "保護者氏名";
//        $header[3]["ZIPCD"] = "郵便番号";
//        $header[3]["ADDR1"] = "現住所";
//        if ($this->z010Name1 == 'bunkyo') {
//            $header[3]["ADDR2"] = "現住所２";
//        }
//        $header[3]["TEL"] = "電話番号";
//        $header[4] = array( "HR_NAME"           => "年組",
//                            "SCHREGNO"          => $schregnotext4,
//                            "ATTENDNO"          => "出席番号",
//                            "NAME"              => "氏名",
//                            "NAME_KANA"         => $namekanatext4,
//                            "SEX"               => "性別",
//                            "BIRTHDAY"          => "生年月日",
//                            "ENT_DIV_NAME"      => "入学種別"
//                          );


        //ヘッダをセット
//        $contents .= implode($header[$this->field["FORM"]], ",")."\n";
//ヘッダは上記ヘッダ毎に個別設定

        //DB接続
        $db = Query::dbCheckOut();
        $companywk = "";
        $company = array();
        if ($this->field["OUTSIDE_TRIALTEST1"] == "1" || $this->field["OUTSIDE_TRIALTEST2"] == "1") {
            if ($this->field["OUTSIDE_TRIALTEST1"] == "1") {
                $companywk = "'".$this->field["TRIALTEST1_NAME"];
            } else {
                $companywk = "'";
            }
            if ($this->field["OUTSIDE_TRIALTEST1"] == "1" && $this->field["OUTSIDE_TRIALTEST2"] == "1") {
                $companywk .= "','";
            }
            if ($this->field["OUTSIDE_TRIALTEST2"] == "1") {
                $companywk .= $this->field["TRIALTEST2_NAME"]."'";
            } else {
                $companywk .= "'";
            }
            $query = knjh344Query::getTrialInfoQuery($companywk);

            $result = $db->query($query);
            while ($nrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($nrow["MOCKCD"] == $this->field["TRIALTEST1_NAME"]) {
                    $company[0] = $nrow["COMPANYCD"];
                } else if ($nrow["MOCKCD"] == $this->field["TRIALTEST2_NAME"]) {
                    $company[1] = $nrow["COMPANYCD"];
                }
            }
        }

        //印刷対象項目
        $item = array("TEL", "E_TEL", "SEX", "BIRTHDAY");

        //データをセット
        $query = knjh344Query::getCsvQuery($this, $company);
//var_dump($query);
//die();
        $result = $db->query($query);
        $data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ項目のみセット
//            foreach ($header[$this->field["FORM"]] as $key => $val) {
            foreach ($headeridx as $ii) {
//print_r("<br>\n");
//var_dump($ii);
//print_r("<br>\n");
                foreach ($header[$ii] as $key => $val) {
//var_dump($val);
                    //和暦変換
                    if ($key == "BIRTHDAY") $row[$key] = str_replace("-","/",$row[$key]);
                    //印刷対象項目チェックなしのデータ
                    //if (in_array($key, $item) && $this->field[$key] == "") $row[$key] = "";
                    $contents .= $row[$key].",";
                }
            }
            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);
//var_dump($this->field["GRADE_HR_CLASS"]);
//die();
        if ($data) {
            $couse = $this->sch_label."名簿";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
