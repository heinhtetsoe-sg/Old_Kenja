<?php

require_once('for_php7.php');

class knjf346aModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $staffcd;//職員コード

    public $selectleft;

    public function knjf346aModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->staffcd = STAFFCD;  //職員コード
        $this->title   = '結核検診再検査報告';

        $this->field = array(
            "YEAR"   => VARS::post("YEAR"),
            "DBNAME" => VARS::post("DBNAME"),
        );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }

    /**
     * 引数のSQLでDBからデータ取得
     */
    private function getDataWithSql($db, $sql)
    {
        $ret = array();

        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ret[]= $row;
        }
        $result->free();

        return $ret;
    }

    /**
     * CSV文字列を取得
     */
    private function getCsvContent()
    {
        $CSV_EL = "\r\n";//CSVとしての改行コード

        //タイトル行を追加
        $headerTitle = array(
            '学校名'     ,
            '学年'       ,
            '生徒氏名'   ,
            '判定'       ,
            '受検年月日' ,
        );
        $contents .= implode(',', $headerTitle) . $CSV_EL;

        //DBからデータ取得
        $db2 = Query::dbCheckOut2();
        $data = $this->getDataWithSql($db2, knjf346aQuery::getCsvData($this));
        Query::dbCheckIn($db2);

        //取得データ行ごとの処理
        $csvColumnKeys = array(//CSV出力する列名
            'SCHOOLNAME'     ,
            'GRADE'          ,
            'NAME'           ,
            'JUDGEMENT'      ,
            'JUDGEMENT_DATE' ,
        );

        foreach ($data as $datum) {
            //データ行ごとにCSV行を追加
            $lineData = array();
            foreach ($csvColumnKeys as $keyName) {
                if ($keyName == "JUDGEMENT_DATE") {
                    $lineData[] = str_replace("-", "/", $datum[$keyName]);
                } else {
                    $lineData[] = $datum[$keyName];
                }
            }
            $contents .= implode(',', $lineData) . $CSV_EL;
        }
        return $contents;
    }

    /**
     * CSVダウンロード
     */
    public function getDownloadModel()
    {
        $filename = sprintf("%s.csv", $this->title);
        $contents = $this->getCsvContent();

        common::downloadContents($contents, $filename, 'SJIS-win');

        return true;
    }
}
