<?php

require_once('for_php7.php');

class knji100c_13Model extends Model {

    var $cmd;
    var $schregno;                  //学籍番号
    var $control_data = array();    //コントロールマスタデータを保持
    var $item;
    var $selectdata_l;                //対象生徒
    var $selectdata_r;                //書出し項目一覧
    var $mode;                      //生徒区分（在校生・卒業生）

    function knja150Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->item = array("EXPENSE"      => "校納金情報"
                            );
        //ITEM
        $this->selectdata_r     = VARS::post("selectdata_r");
        //対象生徒
        $this->selectdata_l     = VARS::post("selectdata_l");
        //学年
        $this->grade            = VARS::post("GRADE");
        //年組
        $this->grade_hr_class   = VARS::post("GRADE_HR_CLASS");
        //異動対象日付
        $this->date             = VARS::post("DATE");
        //出力設定
        $this->output           = VARS::post("OUTPUT");

        //サブシステム
        $this->subsystem = (VARS::get("SUBSYSTEM")) ? VARS::get("SUBSYSTEM") : '13';
        //対象生徒
        if(VARS::get("selectdata_l"))
            $this->selectdata_l     = VARS::get("selectdata_l");
        //年組
        if(VARS::get("GRADE_HR_CLASS"))
            $this->grade_hr_class   = VARS::get("GRADE_HR_CLASS");
        //出力設定
        if(VARS::get("OUTPUT"))
            $this->output = VARS::get("OUTPUT");
    }

    //CSV書出し
    function &getCsvModel() {
        $db = Query::dbCheckOut();

        $contents = "";
        $subTitle = "";
        //ヘッダ作成
        if ($this->output == OUT_CODE_NAME) {
            $contents .= ",,,,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別コード,性別,生徒銀行口座";
        } else {
            $contents .= ",,,,,,";
            $subTitle .= "学籍番号,氏名,学年,組,出席番号,性別,生徒銀行口座";
        }
        //費目別
        $dueTitle = array();
        $dueContents = array();
        $dueSubContents = array();
        $query = knji100c_13Query::selectDueTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dueTitle[$row["YEAR"]][] = $row;
        }
        foreach ($dueTitle as $year => $moneyDue) {
            $firstFlg = true;
            foreach ($moneyDue as $ren => $dueData) {
                $setYear = $firstFlg ? $year."年度" : "";
                $dueContents[$year] .= $firstFlg ? ",{$setYear},,,,,,,," : ",,,,,,,,,";
                $dueCd = $dueData["EXPENSE_L_CD"].$dueData["EXPENSE_M_CD"];
                $dueSubContents[$year] .= ",{$dueCd},{$dueData["EXPENSE_M_NAME"]},費目別入金必要額,入金額,入金日,入金区分,返金額,返金日,返金区分";
                $firstFlg = false;
            }
        }

        //軽減
        $reducTitle = array();
        $reducContents = array();
        $reducSubContents = array();
        $query = knji100c_13Query::selectReducTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $reducTitle[$row["YEAR"]] = $row;
        }
        foreach ($reducTitle as $year => $val) {
            $reducContents[$year] .= ",,,";
            $reducSubContents[$year] .= ",国支援金,府県補助金,軽減額合計";
        }

        //申込
        $applicationTitle = array();
        $applicationContents = array();
        $applicationSubContents = array();
        $query = knji100c_13Query::selectApplicationTitleQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $applicationTitle[$row["YEAR"]][] = $row;
        }
        foreach ($applicationTitle as $year => $appVal) {
            foreach ($appVal as $key => $val) {
                $applicationContents[$year] .= ",,";
                $applicationSubContents[$year] .= ",申込名称,振込金額";
            }
            if (get_count($appVal) > 0) {
                $applicationContents[$year] .= ",";
                $applicationSubContents[$year] .= ",振込金額合計";
            }
        }

        //タイトルセット
        foreach ($dueContents as $key => $val) {
            $contents .= $val.$reducContents[$key].$applicationContents[$key];
        }
        $contents .= "\n";
        $contents .= $subTitle;

        //サブタイトルセット
        foreach ($dueSubContents as $key => $val) {
            $contents .= $val.$reducSubContents[$key].$applicationSubContents[$key];
        }
        $contents .= "\n";
        $schArray = explode(",", $this->selectdata_l);

        foreach ($schArray as $key => $schregno) {
            $headFlg = true;
            $sep = "";
            if (get_count($dueTitle) > 0) {
                foreach ($dueTitle as $year => $moneyDue) {
                    foreach ($moneyDue as $ren => $dueData) {
                        $query = knji100c_13Query::getDueCsvQuery($this, $year, $schregno, $dueData["EXPENSE_L_CD"], $dueData["EXPENSE_M_CD"], $headFlg);
                        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        if ($headFlg) {
                            $contents .= $sep.$row["SCHREGNO"];
                            $sep = ",";
                            $contents .= $sep.$row["NAME"];
                            $contents .= $sep.$row["GRADE"];
                            $contents .= $sep.$row["HR_CLASS"];
                            $contents .= $sep.$row["ATTENDNO"];
                            if ($this->output == OUT_CODE_NAME) {       //コード＋名称
                                $contents .= $sep.$row["SEX"];
                                $contents .= $sep.$row["NAME1"];
                            } else if ($this->output == OUT_CODE_ONLY) { //コードのみ
                                $contents .= $sep.$row["SEX"];
                            } else if ($this->output == OUT_NAME_ONLY) { //名称のみ
                                $contents .= $sep.$row["NAME1"];
                            }
                            $contents .= $sep.$row["ACCOUNTNO"];
                        }
                        $sep = ",";
                        $contents .= $sep.$row["LMCD"];
                        $contents .= $sep.$row["EXPENSE_M_NAME"];
                        $contents .= $sep.$row["MONEY_DUE"];
                        $contents .= $sep.$row["PAID_MONEY"];
                        $contents .= $sep.$row["PAID_MONEY_DATE"];
                        $contents .= $sep.$row["PAID_MONEY_DIV"];
                        $contents .= $sep.$row["REPAY_MONEY"];
                        $contents .= $sep.$row["REPAY_DATE"];
                        $contents .= $sep.$row["REPAY_DEV"];
                        $headFlg = false;
                    }
                    $query = knji100c_13Query::getReducCsvQuery($this, $year, $schregno);
                    $money1 = $db->getOne($query);
                    $query = knji100c_13Query::getReducCountryCsvQuery($this, $year, $schregno);
                    $money2 = $db->getOne($query);
                    $contents .= $sep.$money2.",".$money1.",".($money1 + $money2);
                    $appTotal = 0;
                    if (is_array($applicationTitle[$year])) {
                        foreach ($applicationTitle[$year] as $key1 => $appVal) {
                            $appCd = strlen($appVal["APPLICATIONCD"]) > 0 ? sprintf('%04d', $appVal["APPLICATIONCD"]) : "aaaa";
                            $query = knji100c_13Query::getApplicationCsvQuery($this, $year, $schregno, $appCd);
                            $result = $db->query($query);
                            while ($appRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                                $appRow = str_replace(",", ".", $appRow); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                                $contents .= $sep.$appRow["APPLICATIONNAME"];
                                $contents .= $sep.$appRow["APPLI_PAID_MONEY"];
                                $appTotal += $appRow["APPLI_PAID_MONEY"];
                            }
                            $result->free();
                        }
                    }
                    if (get_count($applicationTitle) > 0) {
                        $contents .= $sep.$appTotal;
                    }
                }
            } else {
                $query = knji100c_13Query::getDueCsvQuery($this, $year, $schregno, "", "", $headFlg);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                unset($row['LMCD']);
                unset($row['EXPENSE_M_NAME']);
                unset($row['EXPENSE_S_EXIST_FLG']);
                unset($row['MONEY_DUE']);
                unset($row['PAID_MONEY']);
                unset($row['PAID_MONEY_DATE']);
                unset($row['PAID_MONEY_DIV']);
                unset($row['REPAY_DEV']);
                unset($row['REPAY_MONEY']);
                unset($row['PAID_INPUT_FLG']);
                unset($row['INST_CD']);
                $contents .= $sep.implode($row, ",");
                $headFlg = false;
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        Query::dbCheckIn($db);
        if ($data){
            $filename = sprintf("%s.csv","校納金情報");
            common::downloadContents($contents, $filename);
            exit;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>