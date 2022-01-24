<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjd232xModel extends Model {
    var $field    = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //職員コード
    var $absent_cov;        //欠課数換算
    var $absent_cov_late;   //欠課数換算遅刻

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjd232xModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "GRADE"             =>  VARS::post("GRADE"),            //学年
                            "GAKKI2"            =>  VARS::post("GAKKI2"),           //学期
                            "ASSESS1"           =>  VARS::post("ASSESS1"),          //評定平均（以上）
                            "ASSESS2"           =>  VARS::post("ASSESS2"),          //成績不振者・評定
                            "COUNT2"            =>  VARS::post("COUNT2"),           //成績不振者・科目数
                            "UNSTUDY2"          =>  VARS::post("UNSTUDY2"),         //成績不振者・未履修科目数
                            "ASSESS_AVE2"       =>  VARS::post("ASSESS_AVE2"),      //成績不振者・評定平均
                            "LATE5"             =>  VARS::post("LATE5"),            //遅刻
                            "ABSENT5"           =>  VARS::post("ABSENT5"),          //欠席
                            "SUBCLASS_ABSENT5"  =>  VARS::post("SUBCLASS_ABSENT5"), //欠課
                            "EARLY5"            =>  VARS::post("EARLY5"),           //早退
                            "DATE"              =>  VARS::post("DATE"),             //異動対象日付
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd       = VARS::request("cmd");
    }

    //成績優良者
    function getDownloadModel1() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "成績判定会議資料・成績優良者詳細リスト";

        $contents = $wrk_nendo ."　" .$title ."\n";

        //出力データの作成//
        //SQL文発行
        $query = knjd232xQuery::selectCsvQuery1($this);
        $result = $db->query($query);
        $data = false;
        $dataTemp = array();
        $tempCnt = array();
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
                        "VALUATION"     => "評定平均"
                        );
        $headCnt = 0;
        foreach ($tempCnt as $key => $val) {
            $headCnt = $headCnt < $val ? $val : $headCnt;
        }

        for ($i = 0; $i < $headCnt; $i++) {
            $header["SUBCLASSNAME".$i] = "科目名";
            $header["VALUE".$i]        = "評定";
            $header["CREDITS".$i]      = "見込単位数";
            $header["COMP_CREDIT".$i]  = "履修単位数";
            $header["GET_CREDIT".$i]   = "修得単位数";
        }

        $contents .= implode($header, ",") ."\n";

        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="成績優良者詳細リスト";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

    //成績不振者
    function getDownloadModel2() {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "成績判定会議資料・成績不振者詳細リスト";

        $contents = $wrk_nendo ."　" .$title ."\n";

        //出力データの作成//
        //SQL文発行
        $query = knjd232xQuery::selectCsvQuery2($this);
        $result = $db->query($query);
        $data = false;
        $dataTemp = array();
        $tempCnt = array();
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
                        "VALUATION"     => "評定平均"
                        );
        $headCnt = 0;
        foreach ($tempCnt as $key => $val) {
            $headCnt = $headCnt < $val ? $val : $headCnt;
        }

        for ($i = 0; $i < $headCnt; $i++) {
            $header["SUBCLASSNAME".$i] = "科目名";
            $header["VALUE".$i]        = "評定";
            $header["CREDITS".$i]      = "見込単位数";
            $header["COMP_CREDIT".$i]  = "履修単位数";
            $header["GET_CREDIT".$i]   = "修得単位数";
        }

        $contents .= implode($header, ",") ."\n";

        foreach ($dataTemp as $key => $val) {
            $contents .= implode($val, ",") ."\n";

            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse="成績不振者詳細リスト";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //出欠状況不振者
    function getDownloadModel3() {
        $db = Query::dbCheckOut();

        //ヘッダの作成//
        $header = array("NUMBER"        => "No.",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "SCHREGNO"      => "学籍番号",
                        "NAME"          => "氏名",
                        "LATE"          => "遅刻",
                        "EARLY"         => "早退",
                        "NOATTEND"      => "欠席",
                        "NOTICE_LATE"   => "欠課数"
                        );

        $wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
        $title = "成績判定会議資料・出欠状況不振者";

        $contents = $wrk_nendo ."　" .$title ."\n";

        $contents .= implode($header, ",") ."\n";

        // 科目別のみ
        // 遅刻何回で欠課とするかの指数取得
        $absent = knjd232xQuery::getScAbsentCov(CTRL_YEAR);
        $this->absent_cov = $absent["ABSENT_COV"];
        $this->absent_cov_late = $absent["ABSENT_COV_LATE"];

        //DB接続
        $db = Query::dbCheckOut();

        //出力データの作成//
        //SQL文発行
        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd232xQuery::getAttendDate($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (str_replace("/","-",$this->field["DATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }
        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjd232xQuery::checkSchoolMst()) > 0) {
            $schoolcd = sprintf("%012d", SCHOOLCD);
            $school_kind = $db->getOne(knjd232xQuery::getSchoolKind($this));
        }
        //SCHOOL_MSTの情報を取得。
        $year = CTRL_YEAR;
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, $year, $schoolcd, $school_kind);
        $query = knjd232xQuery::selectCsvQuery3($this,$attend_seme,$attend_month,$attend_sdate,$knjSchoolMst);

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

        if ($data){
            $couse = "出欠状況不振者";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
