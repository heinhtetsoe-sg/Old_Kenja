<?php

require_once('for_php7.php');
class knjb226Model extends Model
{
    public $field      = array();
    public $selectdata = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjb226Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "RISHUU_YEAR"      =>  VARS::request("RISHUU_YEAR"),   //履修年度
                             "RIREKI_CODE"      =>  VARS::post("RIREKI_CODE"),      //履歴コード
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function clean()
    {
    }

    public function getDownloadModel()
    {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        $data = false;
        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダ配列
        $header = array();
        $header[] = "年組";
        $header[] = "出席Ｎｏ";
        $header[] = "生徒番号";
        $header[] = "氏名";
        $header[] = "希望科目数";
        $header[] = "単位数";
        $header[] = $this->lastColumn;
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;
        //データ
        $dataContents  = "";
        $query = knjb226Query::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換

            $dataContents .= $row["HR_NAME"] . $comma;
            $dataContents .= $row["ATTENDNO"] . $comma;
            $dataContents .= $row["SCHREGNO"] . $comma;
            $dataContents .= $row["NAME_SHOW"] . $comma;
            $dataContents .= $row["SUB_CNT"] . $comma;
            $dataContents .= $row["CREDITS_SUM"] . $comma;

            $dataContents .= $this->lastColumn . $lf;
            $data = true;
        }
        $result->free();
        if ($this->field["RIREKI_CODE"]) {
            $this->getDate = $db->getOne(knjb226Query::getRirekiCode($this, "set"));
        }
        
        //DB切断
        Query::dbCheckIn($db);
        $header1  = "";
        $header1 .= "年度：" . $this->year . "　履修年度：" . $this->field["RISHUU_YEAR"] . "　履修登録日：" . $this->getDate . "　作成日：" . str_replace("-", "/", CTRL_DATE) . $lf;
        //CSV出力
        $contents  = "";
        $contents .= $header1;
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $title = "クラス別希望状況一覧";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
