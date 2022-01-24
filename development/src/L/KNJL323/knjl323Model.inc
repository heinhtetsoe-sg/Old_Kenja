<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl323Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl323Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APDIV"		=>  VARS::post("APDIV"),     		//入試制度
                             "TESTDV"       =>  VARS::post("TESTDV"),    		//入試区分
							 "OUTPUT"   	=>  VARS::post("OUTPUT"),    		//印刷対象
                             "SUCTYPE"      =>  VARS::post("SUCTYPE")           //合格種別
                     );

        $this->cmd         = VARS::request("cmd");
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
    function getDownloadModel() {

        //変数
        $contents = "";
        $header_data = false;
        //タイトル
        $nendo = common::DateConv1($this->ObjYear."/04/01", 10);
        $sakuseibi = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        $title = "";
        if ($this->field["OUTPUT"] == "1") $title = "特待生合格者名簿";
        if ($this->field["OUTPUT"] == "2") $title = "不合格者名簿";
        if ($this->field["OUTPUT"] == "3") {
            $subttl = "";
            if ($this->field["SUCTYPE"] == "1") {
                $subttl = "(Ｔ合格)";
            }
            if ($this->field["SUCTYPE"] == "2") {
                $subttl = "(Ｓ合格)";
            }
            $title = "合格者名簿".$subttl;
        }
        if ($this->field["OUTPUT"] == "4") $title = "アップ合格者名簿";
        if ($this->field["OUTPUT"] == "5") $title = "スライド合格者名簿";
        if ($this->field["OUTPUT"] == "6") $title = "非正規合格者名簿";
        //ヘッダ
        $header = array("TEST_NAME"     => "入試区分",
                        "EXAMNO"        => "受験番号",
                        "NAME"          => "氏名",
                        "NAME_KANA"     => "氏名かな",
                        "SEX_NAME"      => "性別",
                        "FS_NAME"       => "出身学校"
                        );
        $headerContents = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //入試区分マスタ
        $testdivArray = array();
        $query = knjl323Query::get_test_div($this->ObjYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivArray[] = $row;
        }
        $result->free();

        //タイトル
        $titleContents = $nendo . "　入学試験" . $title . "　" . $sakuseibi . "\n";
        //名簿データ
        $dataContents = "";
        $query = knjl323Query::getCsvSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $dataContents .= $row["TEST_NAME"] . ",";
            $dataContents .= $row["EXAMNO"] . ",";
            $dataContents .= $row["NAME"] . ",";
            $dataContents .= $row["NAME_KANA"] . ",";
            $dataContents .= $row["SEX_NAME"] . ",";
            $dataContents .= $row["FS_NAME"] . "\n";
        }
        $result->free();
        //出力データ
        if (strlen($dataContents)) {
            $contents .= $titleContents;
            $contents .= $headerContents;
            $contents .= $dataContents;
            $header_data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv",$title);
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
