<?php

require_once('for_php7.php');

class knjb0031_2Model extends Model
{
    public $fields = array();
    public $cmd;
    public $prgid;
    public $auth;
    public $term;
    public $grade_course;
    public $subclasscd;
    public $group;
    public $data_cnt;
    public $control;
    public $lastColumn;//CSVの最後の列の値

    public function knjb0031_2Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("prgid")) {
            $this->prgid = VARS::get("prgid");
            $this->auth = VARS::get("auth");
            $this->term = VARS::get("term");
            $this->grade_course = VARS::get("GRADE_COURSE");
            $this->subclasscd = VARS::get("SUBCLASSCD");
            $this->group = VARS::get("GROUP");
        }

        $this->cmd   = VARS::request("cmd");

        $this->data_cnt = VARS::post("DATA_CNT");

        //初期化
        $this->fields = array();
        if ($this->cmd == 'update' || $this->cmd == 'csv') {
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->fields["CHAIRCD"][]         =  VARS::post("CHAIRCD"."-".$g);
                $this->fields["CHAIRNAME"][]       =  VARS::post("CHAIRNAME"."-".$g);
                $this->fields["CHAIRABBV"][]       =  VARS::post("CHAIRABBV"."-".$g);
                for ($div = 1; $div <= 2; $div++) {
                    $soeji = ($div == 1) ? "_S" : "_F";
                    $maxNo = 3;
                    for ($no = 1; $no <= $maxNo; $no++) {
                        $this->fields["STAFFCD".$soeji.$no][]        =  VARS::post("STAFFCD".$soeji.$no."-".$g);
                        $this->fields["STAFFNAME_SHOW".$soeji.$no][] =  VARS::post("STAFFNAME_SHOW".$soeji.$no."-".$g);
                    }
                }
                $this->fields["UPDATED"][]         =  VARS::post("UPDATED"."-".$g);
            }//for
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($g = 0; $g < $this->data_cnt; $g++) {
            //キーの更新チェック
            if (!strlen($this->fields["CHAIRCD"][$g])) {
                $this->setWarning("MSG301", "(講座コード)");
                return false;
            }
            if (!strlen($this->subclasscd)) {
                $this->setWarning("MSG301", "(科目コード)");
                return false;
            }
            if (sprintf("%d", $this->fields["CHAIRCD"][$g]) < 1) {
                $this->warning = "１以上を入力して下さい。(講座コード)";
                return false;
            }
            //講座コードゼロ埋め
            if (strlen($this->fields["CHAIRCD"][$g]) < 7) {
                $this->fields["CHAIRCD"][$g] = sprintf("%07d", $this->fields["CHAIRCD"][$g]);
            }
            //文字数チェック
            if (strlen($this->fields["CHAIRCD"][$g]) > 7) {
                $this->setWarning("MSG915", "(講座コード) 7バイトまでです。");
                return false;
            } elseif (strlen($this->fields["CHAIRNAME"][$g]) > 30) {
                $this->setWarning("MSG915", "(講座名称) 30バイトまでです。");
                return false;
            } elseif (strlen($this->fields["CHAIRABBV"][$g]) > 15) {
                $this->setWarning("MSG915", "(講座略称) 15バイトまでです。");
                return false;
            }
            //存在チェック
            $db = Query::dbCheckOut();
            $ret_val = $db->getOne(knjb0031_2Query::countChair($this, $g));
            if ($ret_val == 0) {
                $this->setWarning("MSG303", "講座コード：".$this->fields["CHAIRCD"][$g]);
                return false;
            }
            Query::dbCheckIn($db);

            //職員
            //STAFFCDフィールドサイズ変更対応
            $setsize = ($this->Properties["useStaffcdFieldSize"] === '10') ? 10 : 8;
            $zero_ume = ($this->Properties["useStaffcdFieldSize"] === '10') ? "%010s" : "%08s";
            for ($div = 1; $div <= 2; $div++) {
                $soeji = ($div == 1) ? "_S" : "_F";
                $maxNo = 3;
                for ($no = 1; $no <= $maxNo; $no++) {
                    $staffcd = $this->fields["STAFFCD".$soeji.$no][$g];
                    //文字数チェック
                    if (strlen($staffcd) > $setsize) {
                        $this->setWarning("MSG915", "(職員コード) {$setsize}バイトまでです。");
                        return false;
                    }
                    //存在チェック
                    if (strlen($staffcd)) {
                        //職員コードゼロ埋め
                        if (strlen($staffcd) < $setsize) {
                            $staffcd = sprintf($zero_ume, $staffcd);
                            $this->fields["STAFFCD".$soeji.$no][$g] = $staffcd;
                        }
                        $db = Query::dbCheckOut();
                        $ret_val = $db->getOne(knjb0031_2Query::getCntStaff($this, $staffcd));
                        if ($ret_val == 0) {
                            $this->setWarning("MSG303", "(職員マスタなし) \\n職員コード：".$staffcd);
                            return false;
                        }
                        Query::dbCheckIn($db);
                    }
                }
            }
        }//for
        return true;
    }

    //追加および更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjb0031_2Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    // 職員ＣＳＶ
    public function getDownloadModel()
    {
        $lf = "\n";
        $comma = ",";
        // ＣＳＶ項目
        $header = array();
        $header["YEAR"]       = "※年度";
        $header["SEMESTER"]   = "※学期";
        $header["CHAIRCD"]    = "※講座コード";
        $header["CHAIRNAME"]  = "講座名称";
        $header["CHAIRABBV"]  = "講座略称";
        for ($div = 1; $div <= 2; $div++) {
            $soeji = ($div == 1) ? "_S" : "_F";
            $moji  = ($div == 1) ? "[正]" : "[副]";
            //職員の列数
            $maxNo = 3;
            for ($no = 1; $no <= $maxNo; $no++) {
                $header["STAFFCD".$soeji.$no]        = $moji."※職員コード".$no;
                $header["STAFFNAME_SHOW".$soeji.$no] = $moji."職員名称".$no;
            }
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $db = Query::dbCheckOut();

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, $comma).$lf;

        //講座一覧
        $year = substr($this->term, 0, 4);
        $semester = substr($this->term, 5);
        $chairList = array();
        $query = knjb0031_2Query::getChairList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairList[] = $row;
        }
        $result->free();

        foreach ($chairList as $counter => $row) {
            // CSV出力
            $row = str_replace($comma, ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $year .$comma;
            $contents .= $semester .$comma;
            $contents .= $row["CHAIRCD"] .$comma;
            $contents .= $row["CHAIRNAME"] .$comma;
            $contents .= $row["CHAIRABBV"] .$comma;
            //職員
            for ($div = 1; $div <= 2; $div++) {
                $chargediv = ($div == 1) ? "1" : "0";
                $no = 1;
                $result = $db->query(knjb0031_2Query::getStaff($this->term, $row["CHAIRCD"], $chargediv));
                while ($rowStf = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $contents .= $rowStf["STAFFCD"] .$comma;
                    $contents .= $rowStf["STAFFNAME_SHOW"] .$comma;
                    $no++;
                    if ($maxNo < $no) {
                        break;
                    }
                }
                $result->free();
                while ($no <= $maxNo) {
                    $contents .= "" .$comma;
                    $contents .= "" .$comma;
                    $no++;
                }
            }
            $contents .= $this->lastColumn.$lf;
            $header_data = true;
        }

        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", "講座職員データ");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
