<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd619aModel extends Model {
    var $field         = array();
    var $select_data   = array();
    var $opt_subclass  = array();
    var $tableName;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $virus;
        
    /**
     * メンバ変数の初期化
     */
    function knjd619aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),         //年度
                            "SCHKIND"              =>  VARS::post("SCHKIND"),      //校種
                            "GAKKI"                =>  VARS::post("GAKKI"),        //学期
                            "GAKKI_MAX"            =>  VARS::post("GAKKI_MAX"),    //最終学期
                            "COURSE_MAJOR"         =>  VARS::post("COURSE_MAJOR"), //課程学科
                            "TESTKINDCD"           =>  VARS::post("TESTKINDCD"),   //テストコード
                            "ATTENDDATE"           =>  VARS::post("ATTENDDATE"),   //出欠集計日付
                            "KESSEKI_FLG"          =>  VARS::post("KESSEKI_FLG"),  //欠席者は「*」を印字する
                            "DBNAME"               =>  VARS::post("DBNAME"));      //DB変更に備えて
        $this->field["YEAR"]  = $this->field["YEAR"] ? $this->field["YEAR"] : CTRL_YEAR;

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->tableName = 'TESTITEM_MST_COUNTFLG_NEW_SDIV';
        if ($this->Properties["useTestCountflg"]) {
            $this->tableName = $this->Properties["useTestCountflg"];
        }
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->tableName = 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV';
        }
        $this->virus     = ($this->Properties["useVirus"] == 'true') ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * 
     */
    function getDownloadModel()
    {
        $z010name = knjd619aQuery::getZ010Name();
        //DB接続
        $db = Query::dbCheckOut();

        //テスト//////////////////////////////////////////////////////////
        //ヘッダの作成//
        $header = array("YEAR"         => "年度",
                        "SEMESTER"     => "学期",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名",
                        "TESTITEMNAME" => "テスト種別名"
                        );
        $subheader = array("YEAR"         => "出欠集計日付　".$this->field["ATTENDDATE"],
                           "SEMESTER"     => "",
                           "SCHREGNO"     => "",
                           "GRADE"        => "",
                           "HR_CLASS"     => "",
                           "ATTENDNO"     => "",
                           "NAME_SHOW"    => "",
                           "TESTITEMNAME" => ""
                           );

        //SQL文発行（科目コード・科目名の取得）
        $query = knjd619aQuery::getSelectSubClass1($this,$this->field["GAKKI"]);
        $result = $db->query($query);
        $contents = implode($header, ",") .",";
        $subcontents = implode($subheader,",").",";
        $this->opt_subclass = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($this->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                $contents .= $row["SUBCLASSNAME"].","."追指導,"."追指導備考,"."欠課時数,";
                $subcontents .= $row["SUBCLASSCD"].",".",".",".",";
            } else {
                $contents .= $row["SUBCLASSNAME"].","."欠課時数,";
                $subcontents .= $row["SUBCLASSCD"].",".",";
            }
            $this->opt_subclass[] = $row["SUBCLASSCD"];
        }
        $contents .= "合計,平均,ホーム順位";
        $contents .= "\n";
        $contents .= $subcontents;
        $contents .= "\n";
        //出力データの作成//
        $gakkimei = array();
        $query = knjd619aQuery::getSelectSeme($this->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $gakkimei[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        //SQL文発行

        //テスト名の取得
        if (in_array($this->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query = knjd619aQuery::getTestItem($this, "TESTNAME");
            $testname = $db->getOne($query);
        } else if ($this->tableName == 'TESTITEM_MST_COUNTFLG' && $this->field["TESTKINDCD"] == '9900') {
            $testname = '評価成績';
        } else if ($this->field["GAKKI"] == '9') {
            $testname = ($this->field["TESTKINDCD"] == '9901') ? '学年評価' : '学年評定';
        } else {
            $query = knjd619aQuery::getTestItem($this, "TESTNAME");
            $testname = $db->getOne($query);
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd619aQuery::getScAbsentCov($this), DB_FETCHMODE_ASSOC);

        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd619aQuery::getAttendDate($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];

            if (str_replace("/","-",$this->field["ATTENDDATE"]) < $tmp_attend_sdate) {
                break;
            }
            $attend_month[] = $row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if ($attend_sdate == "") {
            //学期開始日
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".$this->field["YEAR"]."' AND SEMESTER = '1'";
            $attend_sdate = $db->getOne($query2);
        } else {
            //次の日
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);
        }

        //学校マスタ
        $knjSchoolMst = array();
        $query = knjd619aQuery::getSchoolMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $knjSchoolMst[$key] = $val;
            }
        }
        $result->free();

        //追指導情報取得
        $sidouRow = array();
        if ($this->tableName == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $sidouRow = $db->getRow(knjd619aQuery::getSidouInputInf($this), DB_FETCHMODE_ASSOC);
        }

        $query = knjd619aQuery::selectCsvQuery1($this, $testname, $absent, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst, $sidouRow, $z010name);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $this->field["YEAR"].",";
            $contents .= $gakkimei[$this->field["GAKKI"]].",";
            for($i=0;$i<get_count($this->opt_subclass);$i++){
                if ($row["TOKUTEN".$i] == "9999"){
                    $row["TOKUTEN".$i] = "-";
                }else if($row["TOKUTEN".$i] == "8888"){
                    $row["TOKUTEN".$i] = "=";
                }else if($row["TOKUTEN".$i] == "7777"){
                    $row["TOKUTEN".$i] = "*"; //欠席者「*」
                }
            }

            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);
        if ($data){
            $couse="テスト毎";
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
