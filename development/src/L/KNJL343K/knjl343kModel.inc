<?php
/**
 * 定数定義
*/

class knjl343kModel extends Model {
    var $cmd;
    var $select_data      = array();
    var $ObjYear;
    var $testdiv;
    var $output;
    var $date;
    var $special_reason_div;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjl343kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->testdiv  = VARS::post("TESTDIV");
        $this->output  = VARS::post("OUTPUT");
        $this->date  = VARS::post("DATE");
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
    }
    //ＣＳＶ出力
    function getDownloadModel()
    {
        $db  = Query::dbCheckOut();

        //基準点
        $contents = "基準点：";
        $result = $db->query(knjl343kQuery::getBoderScore($this));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["SCORE"] = ($row["SCORE"] != null) ? $row["SCORE"] : "XXX";
            $contents .= $row["NAME"] . $row["SCORE"] . "点　";
        }
        $contents .= "\n";
        $result->free();

        //ヘッダ 2005.12.20 m-yama
        $contents .= "塾コード,塾名,受験番号,性別,氏名,合否,コース,国語,算数,社会,理科,合計,通知承諾,出身学校コード,出身学校名,入学区分(1:手続辞退、2:入学、3:入学辞退)\n";

        //明細データ
        $data = false;
        $result = $db->query(knjl343kQuery::getOutputQueryCsv($this));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data)
        {
            $filename = sprintf("%s.csv","入試選考結果一覧");
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
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
