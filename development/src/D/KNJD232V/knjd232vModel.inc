<?php

require_once('for_php7.php');
require_once("AttendAccumulate.php");

class knjd232vModel extends Model {
    var $field    = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //職員コード
    var $absent_cov;        //欠課数換算
    var $absent_cov_late;   //欠課数換算遅刻
    var $jugyou_jisu_flg;   //1：法定時数・2：実時数
    var $school_div_name;   //学校区分名称

    function knjd232vModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("YEAR"                         =>  VARS::post("YEAR"),                         //年度
                            "GAKKI"                         =>  VARS::post("GAKKI"),                        //学期コード
                            "SCHOOL_KIND"                   =>  VARS::post("SCHOOL_KIND"),                  //校種
                            "GRADE"                         =>  VARS::post("GRADE"),                        //学年
                            "MAJOR"                         =>  VARS::post("MAJOR"),                        //学科
                            "GAKKI2"                        =>  VARS::post("GAKKI2"),                       //学期
                            "TESTKIND"                      =>  VARS::post("TESTKIND"),                     //テスト種別
                            "HYOUTEI_OR_HYOUKA"             =>  VARS::post("HYOUTEI_OR_HYOUKA"),            //評定 or 評価 or 得点
                            "SYUKKETU_SYUKEI"               =>  VARS::post("SYUKKETU_SYUKEI"),              //出欠集計範囲
                            "DATE"                          =>  VARS::post("DATE"),                         //異動対象日付
                            "ATTEND_SUBCLASSCD"             =>  VARS::post("ATTEND_SUBCLASSCD"),            //合併元科目表示
                            "COMBINED_SUBCLASSCD"           =>  VARS::post("COMBINED_SUBCLASSCD"),          //合併先科目表示
                            "SYUTURYOKU_HOU"                =>  VARS::post("SYUTURYOKU_HOU"),               //CSV出力方法(1:1行/1人 2:複数行/1人)
                            "SEISEKI_YURYOU_HYOUTEI"        =>  VARS::post("SEISEKI_YURYOU_HYOUTEI"),       //成績優良者/評定平均(以上)
                            "KYOUKA_SOUGOU1"                =>  VARS::post("KYOUKA_SOUGOU1"),               //教科・科目
                            "KYOUKA_SOUGOU2"                =>  VARS::post("KYOUKA_SOUGOU2"),               //総合的な時間
                            "SEISEKI_HUSIN1"                =>  VARS::post("SEISEKI_HUSIN1"),               //成績不振者チェックボックス
                            "SEISEKI_HUSIN2"                =>  VARS::post("SEISEKI_HUSIN2"),               //成績不振者チェックボックス
                            "SEISEKI_HUSIN3"                =>  VARS::post("SEISEKI_HUSIN3"),               //成績不振者チェックボックス
                            "SEISEKI_HUSIN_HYOUTEI_FROM"    =>  VARS::post("SEISEKI_HUSIN_HYOUTEI_FROM"),   //成績不振者 評定
                            "SEISEKI_HUSIN_HYOUTEI_TO"      =>  VARS::post("SEISEKI_HUSIN_HYOUTEI_TO"),     //成績不振者 評定
                            "SYUKKETU_JOUKYOU"              =>  VARS::post("SYUKKETU_JOUKYOU"),             //出欠状況ラジオ
                            "SYUKKETU_SYUKKETU_TIKOKU"      =>  VARS::post("SYUKKETU_SYUKKETU_TIKOKU"),     //出欠状況/出欠状況/遅刻
                            "SYUKKETU_SYUKKETU_SOUTAI"      =>  VARS::post("SYUKKETU_SYUKKETU_SOUTAI"),     //出欠状況/出欠状況/早退
                            "SYUKKETU_SYUKKETU_KESSEKI"     =>  VARS::post("SYUKKETU_SYUKKETU_KESSEKI"),    //出欠状況/出欠状況/欠席
                            "SYUKKETU_KYOUKA_TIKOKU"        =>  VARS::post("SYUKKETU_KYOUKA_TIKOKU"),       //出欠状況/教科・科目/遅刻
                            "SYUKKETU_KYOUKA_SOUTAI"        =>  VARS::post("SYUKKETU_KYOUKA_SOUTAI"),       //出欠状況/教科・科目/早退
                            "SYUKKETU_KYOUKA_KEKKA"         =>  VARS::post("SYUKKETU_KYOUKA_KEKKA"),        //出欠状況/教科・科目/欠課
                            "SYUKKETU_IGAI_TIKOKU"          =>  VARS::post("SYUKKETU_IGAI_TIKOKU"),         //出欠状況/教科以外/遅刻
                            "SYUKKETU_IGAI_SOUTAI"          =>  VARS::post("SYUKKETU_IGAI_SOUTAI"),         //出欠状況/教科以外/早退
                            "SYUKKETU_IGAI_KEKKA"           =>  VARS::post("SYUKKETU_IGAI_KEKKA"),          //出欠状況/教科以外/欠課
                            "KAIKINSYA"                     =>  VARS::post("KAIKINSYA"),                    //皆勤者ラジオ
                            "ZAISEKI_ALL"                   =>  VARS::post("ZAISEKI_ALL"),                  //在籍期間全てチェックボックス
                            "KAIKIN_KAIKIN_TIKOKU"          =>  VARS::post("KAIKIN_KAIKIN_TIKOKU"),         //皆勤者/皆勤者/遅刻
                            "KAIKIN_KAIKIN_SOUTAI"          =>  VARS::post("KAIKIN_KAIKIN_SOUTAI"),         //皆勤者/皆勤者/早退
                            "KAIKIN_KAIKIN_KESSEKI"         =>  VARS::post("KAIKIN_KAIKIN_KESSEKI"),        //皆勤者/皆勤者/欠席
                            "KAIKIN_KAIKIN_KEKKA"           =>  VARS::post("KAIKIN_KAIKIN_KEKKA"),          //皆勤者/皆勤者/欠課
                            "KAIKIN_KAIKIN_JUGYO_TIKOKU"    =>  VARS::post("KAIKIN_KAIKIN_JUGYO_TIKOKU"),   //皆勤者/皆勤者/授業遅刻
                            "KAIKIN_SEIKIN_TIKOKU"          =>  VARS::post("KAIKIN_SEIKIN_TIKOKU"),         //皆勤者/精勤者/遅刻
                            "KAIKIN_SEIKIN_SOUTAI"          =>  VARS::post("KAIKIN_SEIKIN_SOUTAI"),         //皆勤者/精勤者/早退
                            "KAIKIN_SEIKIN_KESSEKI"         =>  VARS::post("KAIKIN_SEIKIN_KESSEKI"),        //皆勤者/精勤者/欠席
                            "KAIKIN_SEIKIN_KEKKA"           =>  VARS::post("KAIKIN_SEIKIN_KEKKA"),          //皆勤者/精勤者/欠課
                            "KAIKIN_SEIKIN_JUGYO_TIKOKU"    =>  VARS::post("KAIKIN_SEIKIN_JUGYO_TIKOKU"),   //皆勤者/精勤者/授業遅刻
                            "KAIKIN_SEIKIN_SHR_TIKOKU"      =>  VARS::post("KAIKIN_SEIKIN_SHR_TIKOKU"),     //皆勤者/精勤者/SHR/遅刻
                            "KAIKIN_SEIKIN_SHR_SOUTAI"      =>  VARS::post("KAIKIN_SEIKIN_SHR_SOUTAI"),     //皆勤者/精勤者/SHR/早退
                            "DBNAME"                        =>  VARS::post("DBNAME")                        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        // 科目別のみ
        // 遅刻何回で欠課とするかの指数取得
        $absent = knjd232vQuery::getScAbsentCov(CTRL_YEAR);
        $this->absent_cov      = $absent["ABSENT_COV"];
        $this->absent_cov_late = $absent["ABSENT_COV_LATE"];
        $this->amari_kuriage   = $absent["AMARI_KURIAGE"];

        $db = Query::dbCheckOut();
        //授業時数フラグ取得
        $query = knjd232vQuery::getJugyouJisuFlg($this);
        $this->jugyou_jisu_flg = $db->getOne($query);
        //学校区分名称取得
        $query = knjd232vQuery::getSchoolDivName();
        $this->school_div_name = $db->getOne($query);
        Query::dbCheckIn($db);

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //成績優良者
    function getDownloadModel1() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $query = knjd232vQuery::getSemesterName($this->field["GAKKI2"]);
        $wrk_gakki = $db->getOne($query);
        $wrk_test = $db->getOne(knjd232vQuery::getTestName($this));
        $title = "成績判定会議資料・成績優良者詳細リスト";

        $contents = "{$wrk_nendo}　{$wrk_gakki}　{$wrk_test}　{$title}\n";

        //出力データの作成//
        //SQL文発行
        $query = knjd232vQuery::selectCsvQuery1($this);
        $result = $db->query($query);
        $data = false;
        $dataTemp = array();
        $tempCnt = array();
        if ($this->field["SYUTURYOKU_HOU"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                if ($dataTemp[$row["SCHREGNO"]] == "") {
                    $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["NAME"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["ST_COMP"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["ST_GET_ADD"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["VALUATION"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["VALUE"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["CREDITS"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                } else {
                    $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["VALUE"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["CREDITS"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];
                    $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                }
                $tempCnt[$row["SCHREGNO"]]++;
            }

            //ヘッダの作成//
            $header = array("SCHREGNO"      => "学籍番号",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "NAME"          => "氏名",
                            "ST_COMP"       => "履修単位数累計",
                            "ST_GET_ADD"    => "修得単位数累計",
                            "VALUATION"     => $this->field["HYOUTEI_OR_HYOUKA"] ."平均"
                            );
            $headCnt = 0;
            foreach ($tempCnt as $key => $val) {
                $headCnt = $headCnt < $val ? $val : $headCnt;
            }

            for ($i = 0; $i < $headCnt; $i++) {
                $header["SUBCLASSNAME".$i] = "科目名";
                $header["VALUE".$i]        = $this->field["HYOUTEI_OR_HYOUKA"];
                $header["CREDITS".$i]      = "見込単位数";
                $header["COMP_CREDIT".$i]  = "履修単位数";
                $header["GET_CREDIT".$i]   = "修得単位数";
            }

            $contents .= implode($header, ",") ."\n";

            foreach ($dataTemp as $key => $val) {
                $contents .= implode($val, ",") ."\n";
                $data = true;
            }
        } else {
            //ヘッダの作成//
            $header = array("SCHREGNO"      => "学籍番号",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "NAME"          => "氏名",
                            "ST_COMP"       => "履修単位数累計",
                            "ST_GET_ADD"    => "修得単位数累計",
                            "VALUATION"     => $this->field["HYOUTEI_OR_HYOUKA"] ."平均",
                            "SUBCLASSNAME"  => "科目名",
                            "VALUE"         => $this->field["HYOUTEI_OR_HYOUKA"],
                            "CREDITS"       => "見込単位数",
                            "COMP_CREDIT"   => "履修単位数",
                            "GET_CREDIT"    => "修得単位数"
                            );
            $contents .= implode($header, ",") ."\n";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $dataTemp["SCHREGNO"]     = $row["SCHREGNO"];
                $dataTemp["GRADE"]        = $row["GRADE"];
                $dataTemp["HR_CLASS"]     = $row["HR_CLASS"];
                $dataTemp["ATTENDNO"]     = $row["ATTENDNO"];
                $dataTemp["NAME"]         = $row["NAME"];
                $dataTemp["ST_COMP"]      = $row["ST_COMP"];
                $dataTemp["ST_GET_ADD"]   = $row["ST_GET_ADD"];
                $dataTemp["VALUATION"]    = $row["VALUATION"];
                $dataTemp["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                $dataTemp["VALUE"]        = $row["VALUE"];
                $dataTemp["CREDITS"]      = $row["CREDITS"];
                $dataTemp["COMP_CREDIT"]  = $row["COMP_CREDIT"];
                $dataTemp["GET_CREDIT"]   = $row["GET_CREDIT"];

                $contents .= implode($dataTemp, ",") ."\n";
                $data = true;
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse = "{$wrk_nendo}　{$wrk_gakki}　{$wrk_test}　成績判定会議資料・成績優良者詳細リスト";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

/****************************************************************************************/
/****************************************************************************************/
/*******            *********************************************************************/
/******* 成績不振者 *********************************************************************/
/*******            *********************************************************************/
/****************************************************************************************/
/****************************************************************************************/
    function getDownloadModel2() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $query = knjd232vQuery::getSemesterName($this->field["GAKKI2"]);
        $wrk_gakki = $db->getOne($query);
        $wrk_test = $db->getOne(knjd232vQuery::getTestName($this));
        if ($this->cmd == 'csv2_1') { //特別活動の場合
            $title = "成績判定会議資料・成績不振者詳細リスト(特別活動)";
        } elseif ($this->cmd == 'csv2') { //教科・科目 or 総合的な時間
            $title = "成績判定会議資料・成績不振者詳細リスト";
            if ($this->field["KYOUKA_SOUGOU1"] == '1') {
                $title .= "(教科・科目)";
            }
            if ($this->field["KYOUKA_SOUGOU2"] == '1') {
                $title .= "(総合的な時間)";
            }
        } else {
            $title = "成績判定会議資料・成績不振者詳細リスト(過去の不認定科目)";
        }

        $contents = "{$wrk_nendo}　{$wrk_gakki}　{$wrk_test}　{$title}\n";

        //出力データの作成//
        //SQL文発行
        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd232vQuery::getAttendDate($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (($this->field["SYUKKETU_SYUKEI"] == "1")  && (str_replace("/","-",$this->control["学期開始日付"][$this->field["GAKKI2"]]) > $tmp_attend_sdate)) {
                continue;
            }
            if (str_replace("/","-",$this->field["DATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["SEMESTER"].$row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if (($this->field["SYUKKETU_SYUKEI"] == "1") && ($attend_sdate == "")) {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='".$this->field["GAKKI2"]."'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }
        //追指導情報取得
        $sidouRow = array();
        $sidouRow = $db->getRow(knjd232vQuery::getSidouInputInf($this), DB_FETCHMODE_ASSOC);
        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjd232vQuery::checkSchoolMst()) > 0) {
            $schoolcd = sprintf("%012d", SCHOOLCD);
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $school_kind = $this->field["SCHOOL_KIND"];
            } else if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $school_kind = SCHOOLKIND;
            } else {
                $school_kind = $db->getOne(knjd232vQuery::getSchoolKind($this));
            }
        }
        //SCHOOL_MSTの情報を取得。
        $year = CTRL_YEAR;
        //DBにちゃんと登録されていないので直接セットする↓↓↓
        $attend_subclass_special_mst = array("001" => "LHR", "002" => "学校行事", "003" => "生徒会行事", "004" => "その他");
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $year, $schoolcd, $school_kind);
        if($this->cmd == 'csv2_1' || $this->cmd == 'csv2'){
            $query = knjd232vQuery::selectCsvQuery2($this,$attend_seme,$attend_month,$attend_sdate,$knjSchoolMst,$sidouRow);
        } else {
            $query = knjd232vQuery::selectCsvQuery2_2($this);
        }
        $result = $db->query($query);
        $data = false;
        $dataTemp = array();
        $tempCnt = array();
/******************************************************************************************/
/** 特別活動の場合 ************************************************************************/
/******************************************************************************************/
        if ($this->cmd == 'csv2_1') { //特別活動の場合
            if ($this->field["SYUTURYOKU_HOU"] == '1') {
                $contents .= "学籍番号,学年,組,出席番号,氏名,{$attend_subclass_special_mst["001"]},{$attend_subclass_special_mst["002"]},{$attend_subclass_special_mst["003"]},{$attend_subclass_special_mst["004"]},計,出席すべき時数\n";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($dataTemp[$row["SCHREGNO"]] == "") {
                        $dataTemp[$row["SCHREGNO"]]["SCHREGNO"]   = $row["SCHREGNO"];
                        $dataTemp[$row["SCHREGNO"]]["GRADE"]      = $row["GRADE"];
                        $dataTemp[$row["SCHREGNO"]]["HR_CLASS"]   = $row["HR_CLASS"];
                        $dataTemp[$row["SCHREGNO"]]["ATTENDNO"]   = $row["ATTENDNO"];
                        $dataTemp[$row["SCHREGNO"]]["NAME"]       = $row["NAME"];

                        $dataTemp[$row["SCHREGNO"]]["SPECIAL_GROUP_CD_001"]  = 0;
                        $dataTemp[$row["SCHREGNO"]]["SPECIAL_GROUP_CD_002"]  = 0;
                        $dataTemp[$row["SCHREGNO"]]["SPECIAL_GROUP_CD_003"]  = 0;
                        $dataTemp[$row["SCHREGNO"]]["SPECIAL_GROUP_CD_004"]  = 0;
                        $dataTemp[$row["SCHREGNO"]]["YASUNDA_LESSON_GOUKEI"] = 0;
                        $dataTemp[$row["SCHREGNO"]]["LESSON_GOUKEI"] = 0;
                    }
                    $dataTemp[$row["SCHREGNO"]]["SPECIAL_GROUP_CD_{$row["SPECIAL_GROUP_CD"]}"] = $row["YASUNDA_LESSON"];
                    $dataTemp[$row["SCHREGNO"]]["YASUNDA_LESSON_GOUKEI"] += $row["YASUNDA_LESSON"];
                    $dataTemp[$row["SCHREGNO"]]["LESSON_GOUKEI"]         += $row["LESSON"];

                    $data = true;
                }
                foreach ($dataTemp as $schregno => $val) {
                    $contents .= implode(",", $val);
                    $contents .= "\n";
                }
            } else {
                $contents .= "学籍番号,学年,組,出席番号,氏名,科目名,欠課時数\n";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    
                    $dataTemp[$row["SCHREGNO"]][$row["SPECIAL_GROUP_CD"]] = array("SCHREGNO"           => $row["SCHREGNO"],
                                                                                  "GRADE"              => $row["GRADE"],
                                                                                  "HR_CLASS"           => $row["HR_CLASS"],
                                                                                  "ATTENDNO"           => $row["ATTENDNO"],
                                                                                  "NAME"               => $row["NAME"],
                                                                                  "SPECIAL_GROUP_CD"   => $row["SPECIAL_GROUP_CD"],
                                                                                  "SPECIAL_GROUP_NAME" => $row["SPECIAL_GROUP_NAME"],
                                                                                  "YASUNDA_LESSON"     => $row["YASUNDA_LESSON"]
                                                                                  );

                    $goukei[$row["SCHREGNO"]]["YASUNDA_LESSON_GOUKEI"] += $row["YASUNDA_LESSON"];
                    $goukei[$row["SCHREGNO"]]["LESSON_GOUKEI"]         += $row["LESSON"];

                    $seito_info[$row["SCHREGNO"]] = $row;

                    $data = true;
                }
                foreach ($dataTemp as $key => $val) {
                    for($cnt = 1; $cnt < 5; $cnt++) {
                        $contents .= $seito_info["$key"]["SCHREGNO"] . ",";
                        $contents .= $seito_info["$key"]["GRADE"] . ",";
                        $contents .= $seito_info["$key"]["HR_CLASS"] . ",";
                        $contents .= $seito_info["$key"]["ATTENDNO"] . ",";
                        $contents .= $seito_info["$key"]["NAME"] . ",";
                        $contents .= $attend_subclass_special_mst["00{$cnt}"] . ",";

                        if (strlen($val["00{$cnt}"]["YASUNDA_LESSON"])) {
                            $contents .= $val["00{$cnt}"]["YASUNDA_LESSON"];
                        } else {
                            $contents .= "0";
                        }
                        $contents .= "\n";
                    }
                    $contents .= $seito_info["$key"]["SCHREGNO"] . ",";
                    $contents .= $seito_info["$key"]["GRADE"] . ",";
                    $contents .= $seito_info["$key"]["HR_CLASS"] . ",";
                    $contents .= $seito_info["$key"]["ATTENDNO"] . ",";
                    $contents .= $seito_info["$key"]["NAME"] . ",";
                    $contents .= "計,";
                    $contents .= $goukei["$key"]["YASUNDA_LESSON_GOUKEI"];
                    $contents .= "\n";

                    $contents .= $seito_info["$key"]["SCHREGNO"] . ",";
                    $contents .= $seito_info["$key"]["GRADE"] . ",";
                    $contents .= $seito_info["$key"]["HR_CLASS"] . ",";
                    $contents .= $seito_info["$key"]["ATTENDNO"] . ",";
                    $contents .= $seito_info["$key"]["NAME"] . ",";
                    $contents .= "出席すべき時数,";
                    $contents .= $goukei["$key"]["LESSON_GOUKEI"];
                    $contents .= "\n";
                }
            }
/******************************************************************************************/
/* 教科・科目 or 総合的な時間 *************************************************************/
/******************************************************************************************/
        } elseif ($this->cmd == 'csv2') {
            if ($this->field["SYUTURYOKU_HOU"] == '1') {
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    if ($dataTemp[$row["SCHREGNO"]] == "") {
                        $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ST_COMP"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ST_GET_ADD"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["VALUATION"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSCD"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $db->getOne(knjd232vQuery::getSubclassStf($this, CTRL_YEAR, $row["SCHREGNO"], $row["SUBCLASSCD"]));
                        $dataTemp[$row["SCHREGNO"]][] = $row["VALUE"];
                        //追指導
                        if ($sidouRow["SIDOU_INPUT"] == '1') {
                            $dataTemp[$row["SCHREGNO"]][] = $row["SIDOU"];
                        }
                        $dataTemp[$row["SCHREGNO"]][] = $row["CREDITS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["KETUJISU"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["TIKOKU_SOUTAI"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NOTICE_LATE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ABSENT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["MOURNING"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUSPEND"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["LESSON"];
                    } else {
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSCD"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $db->getOne(knjd232vQuery::getSubclassStf($this, CTRL_YEAR, $row["SCHREGNO"], $row["SUBCLASSCD"]));
                        $dataTemp[$row["SCHREGNO"]][] = $row["VALUE"];
                        //追指導
                        if ($sidouRow["SIDOU_INPUT"] == '1') {
                            $dataTemp[$row["SCHREGNO"]][] = $row["SIDOU"];
                        }
                        $dataTemp[$row["SCHREGNO"]][] = $row["CREDITS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["KETUJISU"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["TIKOKU_SOUTAI"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NOTICE_LATE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ABSENT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["MOURNING"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUSPEND"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["LESSON"];
                    }
                    $tempCnt[$row["SCHREGNO"]]++;
                }

                //ヘッダの作成//
                $header = array("SCHREGNO"      => "学籍番号",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME"          => "氏名",
                                "ST_COMP"       => "前年度までの履修単位数累計",
                                "ST_GET_ADD"    => "前年度までの修得単位数累計",
                                "VALUATION"     => $this->field["HYOUTEI_OR_HYOUKA"] ."平均"
                                );
                $headCnt = 0;
                foreach ($tempCnt as $key => $val) {
                    $headCnt = $headCnt < $val ? $val : $headCnt;
                }

                for ($i = 0; $i < $headCnt; $i++) {
                    $header["SUBCLASSCD".$i]    = "科目コード";
                    $header["SUBCLASSNAME".$i]  = "科目名";
                    $header["STAFFNAME".$i]     = "科目担当名";
                    $header["VALUE".$i]         = $this->field["HYOUTEI_OR_HYOUKA"];
                    //追指導
                    if ($sidouRow["SIDOU_INPUT"] == '1') {
                        $header["SIDOU".$i]     = "追指導";
                    }
                    $header["CREDITS".$i]       = "見込単位数";
                    $header["COMP_CREDIT".$i]   = "履修単位数";
                    $header["GET_CREDIT".$i]    = "修得単位数";
                    $header["KETUJISU".$i]      = "欠時数";
                    $header["TIKOKU_SOUTAI".$i] = "遅刻・早退";
                    $header["NOTICE_LATE".$i]   = "欠課時数";
                    $header["ABSENT".$i]        = "公欠数";
                    $header["MOURNING".$i]      = "忌引数";
                    $header["SUSPEND".$i]       = "出停数";
                    $header["LESSON".$i]        = "出席すべき時数";
                }

                $contents .= implode($header, ",") ."\n";

                foreach ($dataTemp as $key => $val) {
                    $contents .= implode($val, ",") ."\n";

                    $data = true;
                }
            } else {
                //ヘッダの作成//
                //追指導
                if ($sidouRow["SIDOU_INPUT"] == '1') {
                    $header = array("SCHREGNO"      => "学籍番号",
                                    "GRADE"         => "学年",
                                    "HR_CLASS"      => "組",
                                    "ATTENDNO"      => "出席番号",
                                    "NAME"          => "氏名",
                                    "ST_COMP"       => "前年度までの履修単位数累計",
                                    "ST_GET_ADD"    => "前年度までの修得単位数累計",
                                    "VALUATION"     => $this->field["HYOUTEI_OR_HYOUKA"] ."平均",
                                    "SUBCLASSCD"    => "科目コード",
                                    "SUBCLASSNAME"  => "科目名",
                                    "STAFFNAME"     => "科目担当名",
                                    "VALUE"         => $this->field["HYOUTEI_OR_HYOUKA"],
                                    "SIDOU"         => "追指導",
                                    "CREDITS"       => "見込単位数",
                                    "COMP_CREDIT"   => "履修単位数",
                                    "GET_CREDIT"    => "修得単位数",
                                    "KETUJISU"      => "欠時数",
                                    "TIKOKU_SOUTAI" => "遅刻・早退",
                                    "NOTICE_LATE"   => "欠課時数",
                                    "ABSENT"        => "公欠数",
                                    "MOURNING"      => "忌引数",
                                    "SUSPEND"       => "出停数",
                                    "LESSON"        => "出席すべき時数"
                                    );
                } else {
                    $header = array("SCHREGNO"      => "学籍番号",
                                    "GRADE"         => "学年",
                                    "HR_CLASS"      => "組",
                                    "ATTENDNO"      => "出席番号",
                                    "NAME"          => "氏名",
                                    "ST_COMP"       => "前年度までの履修単位数累計",
                                    "ST_GET_ADD"    => "前年度までの修得単位数累計",
                                    "VALUATION"     => $this->field["HYOUTEI_OR_HYOUKA"] ."平均",
                                    "SUBCLASSCD"    => "科目コード",
                                    "SUBCLASSNAME"  => "科目名",
                                    "STAFFNAME"     => "科目担当名",
                                    "VALUE"         => $this->field["HYOUTEI_OR_HYOUKA"],
                                    "CREDITS"       => "見込単位数",
                                    "COMP_CREDIT"   => "履修単位数",
                                    "GET_CREDIT"    => "修得単位数",
                                    "KETUJISU"      => "欠時数",
                                    "TIKOKU_SOUTAI" => "遅刻・早退",
                                    "NOTICE_LATE"   => "欠課時数",
                                    "ABSENT"        => "公欠数",
                                    "MOURNING"      => "忌引数",
                                    "SUSPEND"       => "出停数",
                                    "LESSON"        => "出席すべき時数"
                                    );
                }
                $contents .= implode($header, ",") ."\n";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $dataTemp["SCHREGNO"]       = $row["SCHREGNO"];
                    $dataTemp["GRADE"]          = $row["GRADE"];
                    $dataTemp["HR_CLASS"]       = $row["HR_CLASS"];
                    $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                    $dataTemp["NAME"]           = $row["NAME"];
                    $dataTemp["ST_COMP"]        = $row["ST_COMP"];
                    $dataTemp["ST_GET_ADD"]     = $row["ST_GET_ADD"];
                    $dataTemp["VALUATION"]      = $row["VALUATION"];
                    $dataTemp["SUBCLASSCD"]     = $row["SUBCLASSCD"];
                    $dataTemp["SUBCLASSNAME"]   = $row["SUBCLASSNAME"];
                    $dataTemp["STAFFNAME"]      = $db->getOne(knjd232vQuery::getSubclassStf($this, CTRL_YEAR, $row["SCHREGNO"], $row["SUBCLASSCD"]));
                    $dataTemp["VALUE"]          = $row["VALUE"];
                    //追指導
                    if ($sidouRow["SIDOU_INPUT"] == '1') {
                        $dataTemp["SIDOU"]      = $row["SIDOU"];
                    }
                    $dataTemp["CREDITS"]        = $row["CREDITS"];
                    $dataTemp["COMP_CREDIT"]    = $row["COMP_CREDIT"];
                    $dataTemp["GET_CREDIT"]     = $row["GET_CREDIT"];
                    $dataTemp["KETUJISU"]       = $row["KETUJISU"];
                    $dataTemp["TIKOKU_SOUTAI"]  = $row["TIKOKU_SOUTAI"];
                    $dataTemp["NOTICE_LATE"]    = $row["NOTICE_LATE"];
                    $dataTemp["ABSENT"]         = $row["ABSENT"];
                    $dataTemp["MOURNING"]       = $row["MOURNING"];
                    $dataTemp["SUSPEND"]        = $row["SUSPEND"];
                    $dataTemp["LESSON"]         = $row["LESSON"];

                    $contents .= implode($dataTemp, ",") ."\n";
                    $data = true;
                }
            }
/******************************************************************************************/
/* 過去の不認定科目 ***********************************************************************/
/******************************************************************************************/
        } else {
            if ($this->field["SYUTURYOKU_HOU"] == '1') {
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    if ($dataTemp[$row["SCHREGNO"]] == "") {
                        $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["YEAR"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSCD"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["VALUE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["CREDITS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["KETUJISU"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["TIKOKU_SOUTAI"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NOTICE_LATE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ABSENT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["MOURNING"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUSPEND"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["LESSON"];
                    } else {
                        $dataTemp[$row["SCHREGNO"]][] = $row["YEAR"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSCD"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["VALUE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["CREDITS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["COMP_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["GET_CREDIT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["KETUJISU"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["TIKOKU_SOUTAI"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NOTICE_LATE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ABSENT"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["MOURNING"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUSPEND"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["LESSON"];
                    }
                    $tempCnt[$row["SCHREGNO"]]++;
                }

                //ヘッダの作成//
                $header = array("SCHREGNO"      => "学籍番号",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME"          => "氏名"
                                );
                $headCnt = 0;
                foreach ($tempCnt as $key => $val) {
                    $headCnt = $headCnt < $val ? $val : $headCnt;
                }

                for ($i = 0; $i < $headCnt; $i++) {
                    $header["YEAR".$i]          = "年度";
                    $header["SUBCLASSCD".$i]    = "科目コード";
                    $header["SUBCLASSNAME".$i]  = "科目名";
                    $header["VALUE".$i]         = "評定";
                    $header["CREDITS".$i]       = "見込単位数";
                    $header["COMP_CREDIT".$i]   = "履修単位数";
                    $header["GET_CREDIT".$i]    = "修得単位数";
                    $header["KETUJISU".$i]      = "欠時数";
                    $header["TIKOKU_SOUTAI".$i] = "遅刻・早退";
                    $header["NOTICE_LATE".$i]   = "欠課時数";
                    $header["ABSENT".$i]        = "公欠数";
                    $header["MOURNING".$i]      = "忌引数";
                    $header["SUSPEND".$i]       = "出停数";
                    $header["LESSON".$i]        = "出席すべき時数";
                }

                $contents .= implode($header, ",") ."\n";

                foreach ($dataTemp as $key => $val) {
                    $contents .= implode($val, ",") ."\n";

                    $data = true;
                }
            } else {
                //ヘッダの作成//
                $header = array("SCHREGNO"      => "学籍番号",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME"          => "氏名",

                                "YEAR"          => "年度",
                                "SUBCLASSCD"    => "科目コード",
                                "SUBCLASSNAME"  => "科目名",
                                "VALUE"         => "評定",
                                "CREDITS"       => "見込単位数",
                                "COMP_CREDIT"   => "履修単位数",
                                "GET_CREDIT"    => "修得単位数",
                                "KETUJISU"      => "欠時数",
                                "TIKOKU_SOUTAI" => "遅刻・早退",
                                "NOTICE_LATE"   => "欠課時数",
                                "ABSENT"        => "公欠数",
                                "MOURNING"      => "忌引数",
                                "SUSPEND"       => "出停数",
                                "LESSON"        => "出席すべき時数"
                                );

                $contents .= implode($header, ",") ."\n";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $dataTemp["SCHREGNO"]       = $row["SCHREGNO"];
                    $dataTemp["GRADE"]          = $row["GRADE"];
                    $dataTemp["HR_CLASS"]       = $row["HR_CLASS"];
                    $dataTemp["ATTENDNO"]       = $row["ATTENDNO"];
                    $dataTemp["NAME"]           = $row["NAME"];
                    $dataTemp["YEAR"]           = $row["YEAR"];
                    $dataTemp["SUBCLASSCD"]     = $row["SUBCLASSCD"];
                    $dataTemp["SUBCLASSNAME"]   = $row["SUBCLASSNAME"];
                    $dataTemp["VALUE"]          = $row["VALUE"];
                    $dataTemp["CREDITS"]        = $row["CREDITS"];
                    $dataTemp["COMP_CREDIT"]    = $row["COMP_CREDIT"];
                    $dataTemp["GET_CREDIT"]     = $row["GET_CREDIT"];
                    $dataTemp["KETUJISU"]       = $row["KETUJISU"];
                    $dataTemp["TIKOKU_SOUTAI"]  = $row["TIKOKU_SOUTAI"];
                    $dataTemp["NOTICE_LATE"]    = $row["NOTICE_LATE"];
                    $dataTemp["ABSENT"]         = $row["ABSENT"];
                    $dataTemp["MOURNING"]       = $row["MOURNING"];
                    $dataTemp["SUSPEND"]        = $row["SUSPEND"];
                    $dataTemp["LESSON"]         = $row["LESSON"];

                    $contents .= implode($dataTemp, ",") ."\n";
                    $data = true;
                }
            }
        }
/******************************************************************************************/
/******************************************************************************************/
/******************************************************************************************/

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->cmd == 'csv2_1') { //特別活動の場合
                $couse = "成績判定会議資料・成績不振者詳細リスト(特別活動)";
            } elseif ($this->cmd == 'csv2') {
                $couse = "成績判定会議資料・成績不振者詳細リスト";
                if ($this->field["KYOUKA_SOUGOU1"] == '1') {
                    $couse .= "(教科・科目)";
                }
                if ($this->field["KYOUKA_SOUGOU2"] == '1') {
                    $couse .= "(総合的な時間)";
                }
            } else {
                $couse = "成績判定会議資料・成績不振者詳細リスト(過去の不認定科目)";
            }


            $couse = "{$wrk_nendo}　{$wrk_gakki}　{$wrk_test}　{$couse}";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
    //出欠状況不振者
    function getDownloadModel4() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトルに表示するための学年名
        $query = knjd232vQuery::getGradeName($this);
        $gradeName = $db->getOne($query);

        //出力データの作成//
        //SQL文発行
        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd232vQuery::getAttendDate($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (($this->field["SYUKKETU_SYUKEI"] == "1")  && (str_replace("/","-",$this->control["学期開始日付"][$this->field["GAKKI2"]]) > $tmp_attend_sdate)) {
                continue;
            }
            if (str_replace("/","-",$this->field["DATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["SEMESTER"].$row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if (($this->field["SYUKKETU_SYUKEI"] == "1") && ($attend_sdate == "")) {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='".$this->field["GAKKI2"]."'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }
        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjd232vQuery::checkSchoolMst()) > 0) {
            $schoolcd = sprintf("%012d", SCHOOLCD);
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $school_kind = $this->field["SCHOOL_KIND"];
            } else if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $school_kind = SCHOOLKIND;
            } else {
                $school_kind = $db->getOne(knjd232vQuery::getSchoolKind($this));
            }
        }
        //SCHOOL_MSTの情報を取得。
        $year = CTRL_YEAR;
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $year, $schoolcd, $school_kind);
        $query = knjd232vQuery::selectCsvQuery3($this,$attend_seme,$attend_month,$attend_sdate,$knjSchoolMst);

        /************/
        /* 出欠状況 */
        /************/
        if ($this->field["SYUKKETU_JOUKYOU"] == '1') {
            $header = array("NUMBER"        => "No.",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "SCHREGNO"      => "学籍番号",
                            "NAME"          => "氏名",
                            "LATE"          => "遅刻",
                            "EARLY"         => "早退",
                            "NOATTEND"      => "欠席"
                            );

            $isSaitama = $db->getOne(knjd232vQuery::isSaitamasakae()) == 'sakae';

            if ($isSaitama) {
                $header["SYUKKETU"] = '出欠事由';
            }


            $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
            $title = "成績判定会議資料・1日欠席・遅刻・早退状況";
            $contents  = "{$wrk_nendo}　{$gradeName}　{$title}\n";
            $contents .= implode(",", $header) ."\n";

            $result = $db->query($query);
            $data = false;
            $number = 0;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($isSaitama) {
                    $syukketu = '';
                    $result2 = $db->query(knjd232vQuery::getSyukketuCnt($this, $row['SCHREGNO'], $attend_sdate));
                    while( $row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
                        $syukketu .= (isset($sep)?$sep:'').$row2['NAME1'].$row2['CNT'];
                        $sep = '、';
                    }
                    unset($sep);
                }
                $number = $number + 1;
                $contents .= $number.",";
                $contents .= implode($row, ",") .($isSaitama?(','.$syukketu):'')."\n";
                $data = true;
            }
        /************************/
        /* 教科・科目、教科以外 */
        /************************/
        } else {
            $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
            if ($this->field["SYUKKETU_JOUKYOU"] == '2') {
                $title = "成績判定会議資料・出欠状況(教科・科目)";
            } else {
                $title = "成績判定会議資料・出欠状況(教科以外)";
            }
            $contents  = "{$wrk_nendo}　{$gradeName}　{$title}\n";

            $result = $db->query($query);
            $data = false;
            $dataTemp = array();
            $tempCnt = array();
            $number = 0;
            if ($this->field["SYUTURYOKU_HOU"] == '1') {
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    if ($dataTemp[$row["SCHREGNO"]] == "") {
                        $dataTemp[$row["SCHREGNO"]][] = $number;
                        $dataTemp[$row["SCHREGNO"]][] = $row["GRADE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["HR_CLASS"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["ATTENDNO"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SCHREGNO"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["KETSUJI"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["LATE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["EARLY"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NOTICE_LATE"];
                        $number++;
                    } else {
                        $dataTemp[$row["SCHREGNO"]][] = $row["SUBCLASSNAME"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["KETSUJI"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["LATE"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["EARLY"];
                        $dataTemp[$row["SCHREGNO"]][] = $row["NOTICE_LATE"];
                    }
                    $tempCnt[$row["SCHREGNO"]]++;
                }

                //ヘッダの作成//
                $header = array("NUMBER"        => "No.",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "SCHREGNO"      => "学籍番号",
                                "NAME"          => "氏名",
                                );
                $headCnt = 0;
                foreach ($tempCnt as $key => $val) {
                    $headCnt = $headCnt < $val ? $val : $headCnt;
                }

                for ($i = 0; $i < $headCnt; $i++) {
                    $header["SUBCLASSNAME".$i] = "科目名";
                    $header["KETSUJI".$i]      = "欠時数";
                    $header["LATE".$i]         = "遅刻";
                    $header["EARLY".$i]        = "早退";
                    $header["NOTICE_LATE".$i]  = "欠課数";
                }

                $contents .= implode($header, ",") ."\n";

                foreach ($dataTemp as $key => $val) {
                    $contents .= implode($val, ",") ."\n";

                    $data = true;
                }
            } else {
                //ヘッダの作成//
                $header = array("NUMBER"        => "No.",
                                "GRADE"         => "学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "SCHREGNO"      => "学籍番号",
                                "NAME"          => "氏名",
                                "SUBCLASSNAME"  => "科目名",
                                "KETSUJI"       => "欠時数",
                                "LATE"          => "遅刻",
                                "EARLY"         => "早退",
                                "NOTICE_LATE"   => "欠課数",
                                );
                $contents .= implode($header, ",") ."\n";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $dataTemp["NUMBER"]       = $number;
                    $dataTemp["GRADE"]        = $row["GRADE"];
                    $dataTemp["HR_CLASS"]     = $row["HR_CLASS"];
                    $dataTemp["ATTENDNO"]     = $row["ATTENDNO"];
                    $dataTemp["SCHREGNO"]     = $row["SCHREGNO"];
                    $dataTemp["NAME"]         = $row["NAME"];
                    $dataTemp["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                    $dataTemp["KETSUJI"]      = $row["KETSUJI"];
                    $dataTemp["LATE"]         = $row["LATE"];
                    $dataTemp["EARLY"]        = $row["EARLY"];
                    $dataTemp["NOTICE_LATE"]  = $row["NOTICE_LATE"];

                    $number++;
                    $contents .= implode($dataTemp, ",") ."\n";
                    $data = true;
                }
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){

            if ($this->field["SYUKKETU_JOUKYOU"] == '1') {
                $couse = "成績判定会議資料・1日欠席・遅刻・早退状況";
            } elseif ($this->field["SYUKKETU_JOUKYOU"] == '2') {
                $couse = "成績判定会議資料・出欠状況(教科・科目)";
            } else {
                $couse = "成績判定会議資料・出欠状況(教科以外)";
            }
            $couse = "{$wrk_nendo}　{$gradeName}　{$couse}";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }


/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
/***************************************************************************************************************************/
    //皆勤者
    function getDownloadModel5() {
        $db = Query::dbCheckOut();

        //ヘッダの作成//
        if ($this->field["KAIKINSYA"] == '1') {
            $header = array("NUMBER"        => "No.",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "SCHREGNO"      => "学籍番号",
                            "NAME"          => "氏名",
                            "LATE"          => "遅刻",
                            "EARLY"         => "早退",
                            "NOATTEND"      => "欠席",
                            "NOTICE_LATE"   => "欠課数",
                            "SUBCLASS_LATE" => "授業遅刻"
                            );
        } else {
            $header = array("NUMBER"        => "No.",
                            "GRADE"         => "学年",
                            "HR_CLASS"      => "組",
                            "ATTENDNO"      => "出席番号",
                            "SCHREGNO"      => "学籍番号",
                            "NAME"          => "氏名",
                            "LATE"          => "遅刻",
                            "EARLY"         => "早退",
                            "NOATTEND"      => "欠席",
                            "NOTICE_LATE"   => "欠課数",
                            "SUBCLASS_LATE" => "授業遅刻",
                            "SHR_LATE"      => "SHR 遅刻",
                            "SHR_EARLY"     => "SHR 早退"
                            );
        }

        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);

        if ($this->field["KAIKINSYA"] == '1') {
            $title = "成績判定会議資料・皆勤者";
        } else {
            $title = "成績判定会議資料・精勤者";
        }

        $contents = $wrk_nendo ."　" .$title ."\n";

        $contents .= implode(",", $header) ."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成//
        //SQL文発行
        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd232vQuery::getAttendDate($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (($this->field["SYUKKETU_SYUKEI"] == "1")  && (str_replace("/","-",$this->control["学期開始日付"][$this->field["GAKKI2"]]) > $tmp_attend_sdate)) {
                continue;
            }
            if (str_replace("/","-",$this->field["DATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["SEMESTER"].$row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if (($this->field["SYUKKETU_SYUKEI"] == "1") && ($attend_sdate == "")) {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='".$this->field["GAKKI2"]."'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }
        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjd232vQuery::checkSchoolMst()) > 0) {
            $schoolcd = sprintf("%012d", SCHOOLCD);
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $school_kind = $this->field["SCHOOL_KIND"];
            } else if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $school_kind = SCHOOLKIND;
            } else {
                $school_kind = $db->getOne(knjd232vQuery::getSchoolKind($this));
            }
        }
        //SCHOOL_MSTの情報を取得。
        $year = CTRL_YEAR;
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $year, $schoolcd, $school_kind);
        $query = knjd232vQuery::selectCsvQuery3($this,$attend_seme,$attend_month,$attend_sdate,$knjSchoolMst);


        $result = $db->query($query);
        $data = false;
        $number = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $number = $number + 1;
            $contents .= $number.",";
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["KAIKINSYA"] == '1') {
                $couse = "成績判定会議資料・皆勤者";
            } else {
                $couse = "成績判定会議資料・精勤者";
            }
            $couse = "{$wrk_nendo}　{$couse}";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
