<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja260oModel extends Model {
    var $field      = array();
    var $select_data      = array();
    var $opt_subclass      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knja260oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
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

        $this->field = array("YEAR"            	=>  VARS::post("YEAR"),			//年度
                            "GAKKI"         	=>  VARS::post("GAKKI"),		//学期
                            "GAKKI_MAX"         =>  VARS::post("GAKKI_MAX"),	//最終学期
                            "TESTKINDCD"        =>  VARS::post("TESTKINDCD"),	//テストコード
                            "ATTENDDATE"        =>  VARS::post("ATTENDDATE"),	//出欠集計日付
                            "DBNAME"          	=>  VARS::post("DBNAME")		//DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

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
        //DB接続
        $db = Query::dbCheckOut();

        //テスト//////////////////////////////////////////////////////////
        //ヘッダの作成//
        $header = array("YEAR"     	   => "年度",
                        "SEMESTER"     => "学期",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名",
                        "TESTITEMNAME" => "テスト種別名"
                        );
        $subheader = array("YEAR"     	  => "出欠集計日付　".$this->field["ATTENDDATE"],
                           "SEMESTER"     => "",
                           "SCHREGNO"     => "",
                           "GRADE"        => "",
                           "HR_CLASS"     => "",
                           "ATTENDNO"     => "",
                           "NAME_SHOW"    => "",
                           "TESTITEMNAME" => ""
                           );

        //SQL文発行（科目コード・科目名の取得）
        $query = knja260oQuery::getSelectSubClass1($this,$this->field["GAKKI"]);
        $result = $db->query($query);
        $contents = implode($header, ",") .",";
        $subcontents = implode($subheader,",").",";
        $this->opt_subclass = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= $row["SUBCLASSNAME"].","."欠課時数,";
            $subcontents .= $row["SUBCLASSCD"].",".",";
            $this->opt_subclass[] = $row["SUBCLASSCD"];
        }
        $contents .= "\n";
        $contents .= $subcontents;
        $contents .= "\n";
        //出力データの作成//
        $gakkimei = array();
        $query = knja260oQuery::getSelectSeme($this->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $gakkimei[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
//		for($i=0;$i<(int)$this->control["学期数"];$i++){
//			$gakkimei[$i+1] = $this->control["学期名"][$i+1];
//       	}
        //SQL文発行
        $testname = "";
        if ($this->field["TESTKINDCD"] == "01"){
            $testname = "中間試験";
        }else if ($this->field["TESTKINDCD"] == "02"){
            $testname = "中間成績";
        }else if ($this->field["TESTKINDCD"] == "03"){
            $testname = "期末試験";
        }else if ($this->field["TESTKINDCD"] == "04"){
            $testname = "期末成績";
        }else if ($this->field["TESTKINDCD"] == "05"){
            $testname = "期末2試験";
        }else if ($this->field["TESTKINDCD"] == "06"){
            $testname = "期末2成績";
        }else if ($this->field["TESTKINDCD"] == "0"){
            $testname = "学期成績";
        }else {
            $testname = "学年成績";
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knja260oQuery::getScAbsentCov(), DB_FETCHMODE_ASSOC);

        //初期値
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knja260oQuery::getAttendDate($this);
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

        $query = knja260oQuery::selectCsvQuery1($this, $testname, $absent, $attend_seme, $attend_month, $attend_sdate);
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
                }
            }
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="テスト毎";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
