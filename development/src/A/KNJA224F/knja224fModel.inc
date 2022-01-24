<?php

require_once('for_php7.php');

class knja224fModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $isTuusin;

    function knja224fModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "NAME_NASI"         =>  VARS::post("NAME_NASI"),        //名前無し
                             "GAKKI"             =>  VARS::post("GAKKI"),            //学期
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }


    // ＣＳＶ出力
    function getDownloadModel() {

        $header1 = array("HR_CLASS"         => "組",
                         "ATTENDNO"         => "番",
                         "NAME"             => "氏名",
                         "NAME_KANA"        => "ふりがな");

        $db = Query::dbCheckOut();

        $header_data = false;
        $sep = ",";
        $grdClass = "";
        $line = 1;
        
        $contents = "";
        $contents .= ",名列\n";

        $selectdata = array();
        $selectdata = explode(",", $this->selectdata);

        for($grd_cls = 0; $grd_cls < get_count($selectdata); $grd_cls++){

            $query = knja224fQuery::selectCsvQuery($selectdata[$grd_cls], $this);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",", ".", $row);

                if($grdClass != $selectdata[$grd_cls]){
                    if($grdClass != "") $contents .= "\n";
                    //ヘッダ
                    $contents .= ",,".$row["HR_NAME"]."\n";
                    $contents .= implode($header1, ",")."\n";
                    $grdClass = $selectdata[$grd_cls];
                    $line = 1;
                }

                while($line != intval($row["ATTENDNO"])) {
                    //出席番号の歯抜け行を挿入
                    $contents .= ",,,\n";
                    $line = $line + 1;
                }

                //明細
                $contents .= ltrim($row["HR_CLASS"],'0');
                $contents .= $sep.ltrim($row["ATTENDNO"],'0');
                $contents .= $sep.$row["SEX"].$row["NAME"];
                $contents .= $sep.$row["NAME_KANA"];
                $contents .= "\n";

                $line = $line + 1;
                $header_data = true;
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if($header_data) {
            $title = "HRクラス名簿";

            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
