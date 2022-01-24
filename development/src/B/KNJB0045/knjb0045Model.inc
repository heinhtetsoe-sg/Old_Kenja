<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb0045Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $year;   	//年度（通常）
    var $semester;  //学期（通常）
    var $check;  	//チェック実行フラグ
    var $print;  	//印刷フラグ
        
    /**
     * メンバ変数の初期化
     */
    function knjb0045Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->check = "off";
        $this->print = "off";
    }
    function init()
    {               
        $this->field = array("RADIO"           	=>  VARS::post("RADIO"),     		//時間割種別(1:基本,2:通常)
                             "DATE_FROM"        =>  VARS::post("DATE_FROM"),     	//日付FROM
                             "DATE_TO"          =>  VARS::post("DATE_TO"),     		//日付TO
                             "DATE"          	=>  VARS::post("DATE"),     		//名簿日付 04/11/27Add
                             "NENDO"           	=>  VARS::post("NENDO"),     		//年度
                             "TITLE"            =>  VARS::post("TITLE"),     		//タイトル
                             "OPERATION"        =>  VARS::post("OPERATION"),     	//教師稼動数
                             "DBNAME"          	=>  VARS::post("DBNAME")          	//DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
    }
    //名簿の日付チェック 04/11/27Add
    function validateInput2()
    {
	    //学期マスタの存在チェック
        $row = knjb0045Query::getSemesterMst($this->field["DATE"]);
		if (!is_array($row)) {
            $this->warning = "名簿の日付が学期範囲外です。";
            return false;
		}
        return true;
	}
    //日付のチェック
    function validateInput()
    {
	    //開始・終了日付の大小チェック
	    if ($this->field["DATE_FROM"] > $this->field["DATE_TO"]) {
            $this->warning = "日付範囲の大小が正しく有りません。";
            return false;
		}
	    //開始・終了日付の年度・学期がそれぞれ等しいかをチェック
        $row_s = knjb0045Query::getSemesterMst($this->field["DATE_FROM"]);
        $row_e = knjb0045Query::getSemesterMst($this->field["DATE_TO"]);
		if (is_array($row_s) && is_array($row_e)) {
		    //開始・終了日付の年度・学期をそれぞれセット
			if (($row_s["YEAR"] == $row_e["YEAR"]) && ($row_s["SEMESTER"] == $row_e["SEMESTER"])) {
				$this->year 	= $row_s["YEAR"];
				$this->semester = $row_s["SEMESTER"];
			} else {
                $this->warning  = "開始・終了日付の年度または学期が異なっています。";
                $this->warning .= "\\n\\n開始日付：　年度(".$row_s["YEAR"].")、学期(".$row_s["SEMESTER"].")";
                $this->warning .= "\\n終了日付：　年度(".$row_e["YEAR"].")、学期(".$row_e["SEMESTER"].")";
   	            return false;
			}
		} else {
            $this->warning = "開始日付または終了日付が学期範囲外です。";
            return false;
		}
        return true;
	}
    //時間割チェックリストヘッダチェック
    function checkExists($status)
    {
        if ($status == "RUNNING") {
		    //動作状態にRUNNINGがあれば実行しない
    	    if (knjb0045Query::ChecktoStatus($status)) {
        	    $this->warning = "処理中のため実行できません。";
            	return false;
			}
		}
        if ($status == "OK") {
		    //動作状態にOKがなければ実行しない
    	    if (!knjb0045Query::ChecktoStatus($status)) {
        	    $this->warning = "処理済のデータがありません。";
            	return false;
			}
		}
        return true;
	}
    /**
     * チェック実行
     */
    function CheckDate()
    {
		$this->check = "off";//チェック実行フラグ
        if ($this->checkExists("RUNNING")) {
	        if ($this->field["RADIO"] == 2) {	//通常
		        if ($this->validateInput()) $this->check = "on";
			} else {
				if ($this->validateInput2()) $this->check = "on";
	        }
		}
    }
    /**
     * 印刷
     */
    function CheckPrint()
    {
		$this->print = "off";//チェック実行フラグ
        if ($this->checkExists("OK")) $this->print = "on";
    }
    //起動チェック
    function StartCheck()
    {
	    //動作状態にRUNNINGがあればメッセージ表示
   	    if (knjb0045Query::ChecktoStatus("RUNNING")) {
       	    $this->message = "処理中です。";
		}
	}
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
