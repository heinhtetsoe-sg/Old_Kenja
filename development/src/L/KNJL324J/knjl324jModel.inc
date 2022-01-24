<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl324jModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl324jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("TESTDIV"   =>  VARS::post("TESTDIV") );     //入試区分

        $this->cmd   = VARS::request("cmd");
    }

    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();
        $contents = "";
        $data = false;

		//出力データの作成//
        //SQL文発行
        $query = knjl324jQuery::selectCsvQuery($this);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $contents .= $row["L_EXAMNO"] .", ";
            $contents .= $row["C_EXAMNO"] .", ";
            $contents .= $row["R_EXAMNO"] ."\n";

            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


        if ($data){
	        $couse = "合格者発表資料（掲示用）";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

}
?>
