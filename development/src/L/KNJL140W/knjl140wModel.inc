<?php

require_once('for_php7.php');
class knjl140wModel extends Model
{
    public $cmd;
    public $examyear;
    public $outputdiv;
    public $selected_data;
    public $goukaku;
    public $suc_coursecd;
    public $suc_majorcd;
    public $suc_coursecode;
    public $testdiv;
    public $schoolName;
    public $field;
    public $lastColumn;//CSVの最後の列の値

    public function knjl140wModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");
        $this->goukaku       = VARS::post("GOUKAKU");
        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode) = explode(":", VARS::post("GOUKAKU"));

        $this->field = array(
                     "OUTPUTDIV"       => VARS::post("OUTPUTDIV"),      // 出力対象
                     "OUTPUTDIV3TEMPL" => VARS::post("OUTPUTDIV3TEMPL") // テンプレート
                  );
        $db = Query::dbCheckOut();
        $query = knjl140wQuery::getSchoolName();
        $this->schoolName = $db->getOne($query); //学校名取得
        Query::dbCheckIn($db);

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
        if ($this->outputdiv == 1) {
            $dataFlg  = $this->getDownloadStudent($db, $contents);
        } elseif ($this->outputdiv == 2) {
            $dataFlg  = $this->getDownloadGuardian($db, $contents);
            $subtitle = "保護者";
        } else {
            if ($this->field["OUTPUTDIV3TEMPL"] == "1") {
                $dataFlg  = $this->getDownloadTugakuTemplate($db, $contents);
                $subtitle = "通学手段情報テンプレート";
            } else {
                $dataFlg  = $this->getDownloadTugaku($db, $contents);
                $subtitle = "通学手段情報";
            }
        }

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
        $filename = $this->examyear."_入学者".$subtitle.".csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //入学者
    public function getDownloadStudent($db, &$contents)
    {
        $header = array("※入学年度",
                        "※学籍番号",
                        "※入学区分",
                        "学年",
                        "組",
                        "出席番号",
                        "※内外区分",
                        "※課程コード",
                        "※学科コード",
                        "コースコード",
                        "※氏名",
                        "氏名かな",
                        "生年月日",
                        "性別",
                        "出身学校コード",
                        "出身学校卒業日",
                        "郵便番号",
                        "住所１",
                        "住所２",
                        "電話番号",
                        "FAX番号",
                        "EMAIL",
                        "緊急連絡先",
                        "緊急連絡電話番号",
                        "スカラシップ",
                        "課程入学年度",
                        "地区コード",
                        "受験番号",
                        $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl140wQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }

    //保護者
    public function getDownloadGuardian($db, &$contents)
    {
        $header = array("学年",
                        "クラス",
                        "出席番号",
                        "氏名",
                        "※学籍番号",
                        "※続柄",
                        "保護者氏名",
                        "保護者氏名かな",
                        "性別",
                        "生年月日",
                        "郵便番号",
                        "住所1",
                        "住所2",
                        "電話番号",
                        "FAX番号",
                        "E-MAIL",
                        "職種コード",
                        "勤務先名称",
                        "勤務先電話番号",
                        "保証人続柄",
                        "保証人氏名",
                        "保証人氏名かな",
                        "保証人性別",
                        "保証人郵便番号",
                        "保証人住所1",
                        "保証人住所2",
                        "保証人電話番号",
                        "保証人職種コード",
                        "兼ねている公職",
                        $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        $query  = knjl140wQuery::selectCvsGuardQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }

    //通学手段情報テンプレート
    public function getDownloadTugakuTemplate($db, &$contents)
    {
        $contents   ="※CSVCD,※年度,学年,組,出席番号,";
        $contents .="※学籍番号,氏名,災害時帰宅グループ番号,通学所要時間,通学所要分,最寄駅までの手段,";
        $contents .="最寄駅（自宅）,最寄駅名（自宅）,路線（自宅）,路線名（自宅）,下車駅（自宅）,下車駅名（自宅）,通学手段（自宅）,";
        $contents .="乗車駅1,乗車駅名1,路線1,路線名1,下車駅1,下車駅名1,通学手段1,";
        $contents .="乗車駅2,乗車駅名2,路線2,路線名2,下車駅2,下車駅名2,通学手段2,";
        $contents .="乗車駅3,乗車駅名3,路線3,路線名3,下車駅3,下車駅名3,通学手段3,";
        $contents .="乗車駅4,乗車駅名4,路線4,路線名4,下車駅4,下車駅名4,通学手段4,";
        $contents .="乗車駅5,乗車駅名5,路線5,路線名5,下車駅5,下車駅名5,通学手段5,";
        $contents .="最寄駅（学校）,最寄駅名（学校）,路線（学校）,路線名（学校）,下車駅（学校）,下車駅名（学校）,通学手段（学校）,".$this->lastColumn."\n";
        $contents .="3,2005,03,001,001,";
        $contents .="20050101,山田　花子,01,01,30,1,";
        $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,乗車駅,,路線,,下車駅,,2,";
        $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,乗車駅,,路線,,下車駅,,2,";
        $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,乗車駅,,路線,,下車駅,,2,";
        $contents .="9999999,○○駅,99999,■■線,9999999,△△駅,1,".$this->lastColumn;

        $dataFlg = true;

        return $dataFlg;
    }

    //通学手段情報
    public function getDownloadTugaku($db, &$contents)
    {
        $contents   ="※CSVCD,※年度,学年,組,出席番号,";
        $contents .="※学籍番号,氏名,災害時帰宅グループ番号,通学所要時間,通学所要分,最寄駅までの手段,";
        $contents .="最寄駅（自宅）,最寄駅名（自宅）,路線（自宅）,路線名（自宅）,下車駅（自宅）,下車駅名（自宅）,通学手段（自宅）,";
        $contents .="乗車駅1,乗車駅名1,路線1,路線名1,下車駅1,下車駅名1,通学手段1,";
        $contents .="乗車駅2,乗車駅名2,路線2,路線名2,下車駅2,下車駅名2,通学手段2,";
        $contents .="乗車駅3,乗車駅名3,路線3,路線名3,下車駅3,下車駅名3,通学手段3,";
        $contents .="乗車駅4,乗車駅名4,路線4,路線名4,下車駅4,下車駅名4,通学手段4,";
        $contents .="乗車駅5,乗車駅名5,路線5,路線名5,下車駅5,下車駅名5,通学手段5,";
        $contents .="最寄駅（学校）,最寄駅名（学校）,路線（学校）,路線名（学校）,下車駅（学校）,下車駅名（学校）,通学手段（学校）,".$this->lastColumn."\n";
        $dataFlg  = false;

        $query  = knjl140wQuery::selectCsvTugakuQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
