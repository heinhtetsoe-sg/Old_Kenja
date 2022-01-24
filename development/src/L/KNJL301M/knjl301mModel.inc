<?php

require_once('for_php7.php');

class knjl301mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $test_year;

    function knjl301mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }
    function init() {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TAISHOU"          =>  VARS::post("TAISHOU"),          //対象者
                             "S_EXAMNO"         =>  VARS::post("S_EXAMNO"),         //受験番号開始
                             "E_EXAMNO"         =>  VARS::post("E_EXAMNO"),         //受験番号終了
                             "SYUTURYOKUJUN"    =>  VARS::post("SYUTURYOKUJUN"),    //出力順
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    function clean() {}

    //ＣＳＶ出力
    function getDownloadModel()
    {
		//ヘッダの作成//
        $header = array("EXAMNO"            => "受験番号",
                        "NAME"              => "生徒氏名",
                        "NAME_KANA"         => "氏名カナ",
                        "BIRTHDAY"          => "生年月日",
                        "FS_NAME"           => "出身学校",
                        "NATPUBPRI_NAME"    => "国公立区分",
                        "AREA_DIV_NAME"     => "所在地区分",
                        "AREA_NAME"         => "所在地",
                        "REMARK1"           => "備考",
                        "GNAME"             => "保護者氏名",
                        "GKANA"             => "保護者カナ"
                        );

        //DB接続
        $db = Query::dbCheckOut();

        //年度取得
        $nxtyr_date = (substr(CTRL_DATE,0,4)+1).(substr(CTRL_DATE,4));
	    $year = common::DateConv1(str_replace("-","/",$nxtyr_date),10);

        //日付・曜日・時間取得
	    $date = common::DateConv1(str_replace("-","/",CTRL_DATE),3).date(" H:i");

        $contents  = $year."　入学志願者チェックリスト\n";
        $contents .= ",,,,,,,,".$date."\n";
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $query = knjl301mQuery::selectCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

	        $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();

        //集計人数
        $boy = knjl301mQuery::selectCsvQuery($this, "1");
        $girl = knjl301mQuery::selectCsvQuery($this, "2");

        $contents .= "\n";
        $contents .= ",,,,,,,,男 ".get_count($db->getcol($boy))."名,女 ".get_count($db->getcol($girl))."名,計 ".get_count($db->getcol($query))."名\n";

        Query::dbCheckIn($db);

        if ($data){
	        $couse="志願者チェックリスト";
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
