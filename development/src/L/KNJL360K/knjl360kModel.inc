<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl360kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $jhflg;
    var $scalashipdiv;
    var $score;
    var $output2;
    var $special_reason_div;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl360kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->jhflg        = VARS::post("JHFLG");
        $this->scalashipdiv = VARS::post("SCALASHIPDIV");
        $this->score        = VARS::post("SCORE");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    function getDownloadModel()
    {
        $header = array("ORDER"              => "対象データ",
                        "EXAMNO"             => "受験番号",
                        "SCALASHIPDIV"       => "対象スカラコード"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjl360kQuery::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ設定
            $contents .= "9,"
                        .$row["EXAMNO"].","
                        ."\n";  //NO001
//                      .$this->scalashipdiv." \n";
            $data = true;
        }
        $result->free();

        $result = $db->query(knjl360kQuery::GetScalashipdiv($this,"2"));
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $scalaname = $row2["NAME1"];
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($this->jhflg == 1){
            $jhname = "中学校";
        }else {
            $jhname = "高等学校";
        }

        if($data){
//          $filename = $this->ObjYear."年度".$jhname.$scalaname."認定.csv";
            $filename = $this->ObjYear."年度".$jhname."スカラシップ認定.csv";   //NO001
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }

}
?>
