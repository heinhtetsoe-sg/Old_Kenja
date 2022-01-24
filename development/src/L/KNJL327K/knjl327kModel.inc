<?php
/**
 * 定数定義
*/

class knjl327kModel extends Model {
    var $cmd;
    var $ObjYear;
    var $errdata;
    var $opt_csv = array();
    var $control;        //コントロールマスタ
    var $special_reason_div;

    /**
     * メンバ変数の初期化
     */
    function knjl327kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        if (VARS::get("PROGRAMID")) {
            $this->headercheck    = "1";
            $this->opt_csv = array();
        }else{
            $this->headercheck    = VARS::post("HEADERCHECK");
        }

        $this->file = VARS::file("FILE");

    }

    //テンプレートの書き出し
    function OutputTmpFile()
    {
        $contents    ="※受験番号\n";
        $contents .="9999\n";
        $filename = sprintf("%s.csv","成績一覧表（抽出用）");

        common::downloadContents($contents, $filename);
        return true;

    }

    function &getUpdateModel()
    {
        //事項時間の制限を無くす
        set_time_limit(0);

        //DB接続
        $db = Query::dbCheckOut();
        $check = array();
        $result = $db->query(knjl327kQuery::GetExamno($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $check[] = $row["VALUE"];
        }
        $result->free();
        //DB切断
        Query::dbCheckIn($db);

        $this->errdata = "";
        $this->opt_csv = array();
        //ファイルの文字列の取得
        $this->opt_csv = $this->GetFileString($check);
        if ($this->errdata) {
            $this->setMessage(MSG202, $this->errdata."行目が不正でした。");
        }

    }

    function GetFileString($check)
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen ($this->file["tmp_name"], "r");
            $header = false;
            $i = 1;
            $seq = "";
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        $i++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $exam   = (strlen(trim($tmp[0]))) ? sprintf("%04d",$tmp[0]) : "";
                    if (in_array($exam, $check)) {
                        $temp_arr[] = $exam;
                    } else {
                        $this->errdata .= $seq.$i;
                        $seq = ",";
                    }
                }
                $i++;
            }
            fclose($fp);

        }
        return $temp_arr;
    }

}
?>
