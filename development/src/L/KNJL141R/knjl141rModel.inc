<?php

require_once('for_php7.php');
class knjl141rModel extends Model
{
    public $cmd;
    public $examyear;
    public $outputdiv;
    public $selected_data;
    public $suc_coursecd;
    public $suc_majorcd;
    public $suc_coursecode;
    public $testdiv;
    public $schoolName;
    public $lastColumn;//CSVの最後の列の値

    public function knjl141rModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");

        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode) = explode(":", VARS::post("GOUKAKU"));

        $db = Query::dbCheckOut();
        $query = knjl141rQuery::getSchoolName();
        $this->schoolName = $db->getOne($query); //学校名取得
        Query::dbCheckIn($db);

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $subtitle = "";
        $dataFlg  = $this->getDownloadStudent($db, $contents);

        if ($dataFlg) {
            //CSV出力
            $this->putCsv($db, $contents, $subtitle);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //CSV出力
    public function putCsv($db, $contents, $subtitle)
    {
        $filename = $this->examyear."_入学者クラス編成結果データ".$subtitle.".csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //合格者
    public function getDownloadStudent($db, &$contents)
    {
    
        //科目ヘッダー情報取得
        $kamokuRow = array();
        $i = 0;
        $query  = knjl141rQuery::getKamokuQuery();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kamokuRow[$i] = $row["NAME1"];
            $i++;
        }
        $result->free();
    
        $header = array("入学年度",
                        "受験番号",
                        "氏名",
                        "氏名かな",
                        "性別コード",
                        "生年月日",
                        "出身学校コード",
                        "出身学校名",
                        
                        "郵便番号",
                        "住所１",
                        "住所２",
                        "電話番号",
                        "保護者氏名",
                        "保護者氏名かな",
                        "続柄",
                        
                        "入試区分",
                        "サブ",
                        "奨学生",
                        "合格・課程コード",
                        "合格・学科コード",
                        "合格・コースコード",
                        "合格コース名",
                        "入学・課程コード",
                        "入学・学科コード",
                        "入学・コースコード",
                        "入学課程学科コース名",
                        "第１希望",
                        "第２希望",
                        "第３希望",
                        
                        $kamokuRow[0],
                        $kamokuRow[1],
                        $kamokuRow[2],
                        $kamokuRow[3],
                        $kamokuRow[4],

                        "内申点90点",
                        "合計",
                        "面接",
                        "欠席日数",
                        
                        $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl141rQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //広島国際は賢者として取込むため、かたかなに変換
            if ($this->schoolName == 'hirogaku') {
                $row["NAME_KANA"] = mb_convert_kana($row["NAME_KANA"], "cHV", "UTF-8");
                $row["GKANA"]     = mb_convert_kana($row["GKANA"], "cHV", "UTF-8");
            }
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
