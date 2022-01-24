<?php

require_once('for_php7.php');

class knjl433mModel extends Model
{
    public $cmd;
    public $examyear;
    public $schoolKind;
    public $examId;

    public function init()
    {
        $this->cmd              = VARS::request("cmd");
        $this->examyear         = CTRL_YEAR + 1;
        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->examId           = VARS::post("EXAM_ID");

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            //機能別に指定された校種を取得
            $this->getSelectSchoolKind();
        }
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["RECEPTNO"] = "受験番号";
        $header["STATUS"]   = "合否";

        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //データ出力
        $dataFlg = false;
        $query = knjl433mQuery::getCsvList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            $title = $this->examyear."年度";
            $title .= "miraicompassアップロード用CSV出力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
