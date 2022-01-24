<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja321Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja321Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("CSV_DIV"          =>  VARS::post("CSV_DIV"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                             "GRADE"            =>  VARS::post("GRADE")
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //テンプレートダウンロード
    function gettemplateDownloadModel() {
        if ($this->field["CSV_DIV"] == "2") {
            $filename = "zaseki.xlsx";
        } else if ($this->field["CSV_DIV"] == "3") {
            $filename = "kinkyu.xlsx";
        } else {
            $filename = "nahyou.xlsx";
        }

        $path_file = DOCUMENTROOT."/download/".$filename;
        //ファイルの存在確認
        if (!file_exists($path_file)) {
            $this->setWarning("MSG303","指定ファイルは存在しません。" + $path_file);
            return false;
        }

        //オープンできるか確認
        if (!($fp = fopen($path_file, "r"))) {
            $this->setWarning("MSG203","指定ファイルは開くことができません。");
            return false;
        }
        fclose($fp);

        //ファイルサイズの確認
        if (($content_length = filesize($path_file)) == 0) {
            $this->setWarning("MSG203","指定ファイルのサイズは 0 byteです。");
            return false;
        }

        ini_set('mbstring.http_output', 'pass');
        //ダウンロード用のHTTPヘッダ送信
        header('Content-Type: application/pdf');
        common::setDownloadHeader($filename);
        header("Content-Length: ".$content_length);

        //ファイルを読んで出力
        if (!readfile($path_file)) {
            $this->setWarning("MSG203","指定ファイルは読むことができません。");
            return false;
        }

        return true;
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $db  = Query::dbCheckOut();

        $data = false;
        $contents = "";
        if($this->field["CSV_DIV"] == '1'){
            $result = $db->query(knja321Query::getGrade($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row['VALUE'] == $this->field["GRADE"]){
                    $contents = $row['VALUE'].','.$row['LABEL'];
                }
            }
        } else {
            $result = $db->query(knja321Query::getGradeHrclass($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row['VALUE'] == $this->field["GRADE_HR_CLASS"]){
                    $contents = $row['LABEL'];
                }
            }
        }
        if($this->field["CSV_DIV"] == '3'){
            $tel = $db->getOne(knja321Query::getSchoolTelNo($this));
        }
        $result = $db->query(knja321Query::getHrClass($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= ','.(($this->field["CSV_DIV"] == '3')?$tel:$row['HR_NAME']).','. $row['TANNNIN'].','.$row['HUKUTANNNIN'];
        }
        $contents .= "\n";
        
        if($this->field["CSV_DIV"] == '1' || $this->field["CSV_DIV"] == '2'){
            //ヘッダ
            $contents .= "座席番号,年度,名票コード,氏名,ふりがな,性別\n";
            
            $result = $db->query(knja321Query::getOutputQueryCsv($this));
        } else {
            //ヘッダ
            $contents .= "座席番号,年度,学年,組,出席番号,氏名,緊急連絡先1,緊急連絡先2\n";
            
            $result = $db->query(knja321Query::getOutputQueryCsv2($this));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        Query::dbCheckIn($db);

        if ($this->field["CSV_DIV"] == "1") {
            $csvTitle = "名票";
        } else if ($this->field["CSV_DIV"] == "2") {
            $csvTitle = "座席表";
        } else {
            $csvTitle = "緊急連絡網";
        }
        if ($data) {
            $filename = sprintf("%s.csv", $csvTitle);
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
