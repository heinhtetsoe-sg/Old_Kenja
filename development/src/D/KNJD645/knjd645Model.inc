<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd645Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;
        
    /**
     * メンバ変数の初期化
     */
    function knjd645Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("YEAR"     =>  VARS::post("YEAR"),     //年度
                             "SEMESTER" =>  VARS::post("SEMESTER"), //学期
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
    }

    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        $contents = ""; //CSVデータ出力用
        $data = false;

        //学部学科ループ
        for($i=0;$i<get_count($this->select_data["selectdata"]);$i++){
            $contents .= $this->getResult($db,$i);
            if($contents!="") $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data){
            $next_year = CTRL_YEAR+1;
            $course   = $next_year."年度　推薦名簿";
            $filename = sprintf("%s.csv",$course);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }   //getDownloadModelの綴り

    //推薦名簿
    function getResult($db,$i)
    {
        $data = false;
        $next_year = CTRL_YEAR+1;
        $contents .= ",".$next_year."年度　　付属校推薦入学　　推薦名簿"."\n";
        $contents .= "\n";

        //学部学科名取得
        $query = knjd645Query::getBuKa_Name($this->select_data["selectdata"][$i]);
        $bukaname = array();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

                $bukaname = $row["LABEL"];
                $contents .= "推薦学部　　".$bukaname.",,,,学校名　　法政大学高等学校\n";

            $data = true;
        }

        $contents .= "受験番号,氏　　名,総合判定順位,総合判定成績,備　　考,志願審査"."\n";

        //推薦名簿取得
        $query = knjd645Query::getList($this, $this->select_data["selectdata"][$i]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $contents .= ",";
            $contents .= $row["NAME"].",";
            $contents .= $row["RANK1"].",";
            $contents .= $row["SCORE1"].",";
            $contents .= $row["SCORE3"].",";
            $contents .= "\n";

            $data = true;
        }

        if($data){
            $contents .= "\n";
            $contents .= "付属校推薦入学推薦名簿　　　推薦学部　".$bukaname."\n";
            $contents .= "\n";
            $contents .= "\n";
        }

        $result->free();

        return $contents;
    }

}
?>
