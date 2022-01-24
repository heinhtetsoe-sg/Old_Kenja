<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje131Model extends Model {
    var $field          = array();
    var $opt_classcd    = array();
    var $opt_subclasscd    = array();
    var $selectdata;
    var $selectdata2;
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knje131Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							"GAKKI"				=>	VARS::post("GAKKI"),	        //学期コード
							"GRADE"				=>	VARS::post("GRADE"),	        //学年
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd          = VARS::request("cmd");
	    $this->selectdata   = VARS::post("selectdata");
	    $this->selectdata2  = VARS::post("selectdata2");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
	/**
	 * ＣＳＶ出力
	 */
    function getDownloadModel()
    {
		//ヘッダ
        $header = array("YEAR"     	   => "年度",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME"         => "生徒氏名",
                        "SEX"          => "性別"
                        );
        $header2 = array("VAL_5ALL"     => "主要教科の評定平均",
                         "VAL_ALL"      => "全教科の評定平均"
                        );

        $contents = implode($header, ",") .",";

        //DB接続
        $db = Query::dbCheckOut();

        //科目
        $query = knje131Query::getSubClassName($this);
        $result = $db->query($query);
		$this->opt_subclasscd = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= $row["SUBCLASSNAME"] . "(" . sprintf("%d",$row["ANNUAL"]) . "年生),";
		    $this->opt_subclasscd[] = $row["SUBCLASSCD"] . "-" . $row["ANNUAL"];
        }

        //教科
        $query = knje131Query::getClassName($this);
        $result = $db->query($query);
		$this->opt_classcd = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= $row["CLASSNAME"] . "平均,";
		    $this->opt_classcd[] = $row["CLASSCD"];
        }
        $contents .= implode($header2, ",") ."\n";

        //性別
        $query = knje131Query::getSexName();
        $result = $db->query($query);
        $sex_name = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $sex_name[$row["NAMECD2"]] = $row["ABBV1"];
        }

        //SQL文発行
        $query = knje131Query::getSelectCsv($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["SEX"] = $sex_name[$row["SEX"]];

            $contents .= CTRL_YEAR . ",";
	        $contents .= implode($row, ",") ."\n";

            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
	        $couse="評定平均値一覧表";
            $filename = sprintf("%s.csv",$couse);
    		common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
