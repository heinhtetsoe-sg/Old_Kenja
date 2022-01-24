<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf323aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $ctrlYear;
    public $staffcd;
    public $ctrlSemester;
    public $schoolcd;
    public $selectdata = array();

    /**
     * メンバ変数の初期化
     */
    public function knjf323aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    public function init()
    {
        $this->field = array("KUBUN"    =>  VARS::post("KUBUN"),  //クラス・個人表示指定
                             "DBNAME"   =>  VARS::post("DBNAME")  //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->ctrlYear = CTRL_YEAR;
        $this->ctrlSemester = CTRL_SEMESTER;
        $this->ctrlDate = CTRL_DATE;
        $this->schoolcd = SCHOOLCD;
        $this->selectdata = explode(",", VARS::post("selectdata"));

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    // ＣＳＶ出力
    public function getDownloadModel()
    {
        $printDate = date("Y年m月d日");

        $header = array("GRADE_HR_CLASS_ATTENDNO" => "年組番",
                        "NAME"                    => "氏名",
                        "SEX"                     => "性別",
                        "GUARD_TELNO"             => "緊急連絡先",
                        "MEDICAL_HISTORY1"        => "既往症１",
                        "MEDICAL_HISTORY2"        => "既往症２",
                        "MEDICAL_HISTORY3"        => "既往症３",
                        "HEARTDISEASE"            => "心臓疾患",
                        "REMARK"                  => "備考");

        $db = Query::dbCheckOut();

        $header_data = false;
        $contents = "";

        $headerList = $dataList = array();
        $query = knjf323aQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            $headerList[$row["PAGE_ITEM_CD"]] = $row["PAGE_ITEM_NAME"];

            foreach ($header as $key => $val) {
                $dataList[$row["PAGE_ITEM_CD"]][$row["GRADE_HR_CLASS_ATTENDNO"]][$key] = $row[$key];
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        foreach ($headerList as $key => $val) {
            $contents .= $this->ctrlYear."年度 ".$printDate." ".$val."\n";

            //ヘッダを出力
            $cma = "";
            foreach ($header as $headerName => $headerValue) {
                $contents .= $cma.$headerValue;
                $cma = ",";
            }
            $contents .= "\n";

            //データを出力
            $itemList = $dataList[$key];
            foreach ($itemList as $attendno => $student) {
                $cma = "";
                foreach ($itemList[$attendno] as $columnName => $columnValue) {
                    $contents .= $cma.$itemList[$attendno][$columnName];
                    $cma = ",";
                    $header_data = true;
                }
                $contents .= "\n";
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            switch ($this->field["KUBUN"]) {
                case "1":
                    $title = $this->ctrlYear."年度　健康リスト（クラス別）";
                    break;
                case "2":
                    $title = $this->ctrlYear."年度　健康リスト（部活動別）";
                    break;
            }

            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
