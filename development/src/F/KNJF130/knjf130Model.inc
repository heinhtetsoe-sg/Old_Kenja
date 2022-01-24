<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf130Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjf130Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),			//年度
                            "DBNAME"          =>  VARS::post("DBNAME")          //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
    }

	/**
	 * ＣＳＶ出力
	 */
    function getDownloadModel()
    {
        //タイトル行 2004/01/26 add nakamoto
		$wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
		$title = "学年時間別来室者状況";
		$wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        $contents = $wrk_nendo ."," .$title .",," ."作成日：" .$wrk_date ."\n";

		//ヘッダの作成//
        $header = array("KUBUN"       => "学年",
                        "NAMECD2"     => "来室校時",
                        "NINZUU4"     => "性別（男子数）",
                        "NINZUU5"     => "性別（女子数）",
                        );

        $contents .= implode($header, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

		//SQL文発行（来室校時名の取得）
        $query = knjf130Query::getSelectName("F700");
        $result = $db->query($query);
		$opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
		//出力データの作成//
        //SQL文発行
        $query = knjf130Query::selectCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["GRADE"] = sprintf("%d",$row["GRADE"])."学年";
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
	        $contents .= implode($row, ",") ."\n";

            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
	        $couse="学年時間別来室者状況";
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
