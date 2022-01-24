<?php

require_once('for_php7.php');

class knjf344Model extends Model
{
    public $cmd;
    public $control_data = array();//コントロールのデータを保持
    public $staffcd;               //セキュリティーチェック
    public $sec_competence;        //権限保持
    public $updated = array();     //UPDATED値保持
    public $schoolcd;              //学校コード
    public $execute_date;          //県への報告用登録日付
    public $docNumber;
    public $rowCount;              //データ行数
    public $title;                 //CSV名、画面名称

    public function knjf344Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->execute_date = VARS::post("EXECUTE_DATE");
        $this->fixedDate    = VARS::post("FIXED_DATE");
        $this->fixedData    = VARS::post("FIXED_DATA");
        $this->rowCount     = VARS::post("ROW_COUNT");
        $this->title        = '心電図一次検査所見者報告';

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, 'KNJF344');

        //画面の入力値を保持
        $this->dataFieldKeys = array(
            'SCHREGNO',
            'GRADE'   ,
            'NAME'    ,
            'RESULT'  ,
            'REMARK'  ,
        );
        $this->updField = array();

        for ($i = 0; $i < $this->rowCount; $i++) {
            $tempArray = array();
            foreach ($this->dataFieldKeys as $keyName) {
                $completeKeyName = sprintf('%08d__', $i) . $keyName;
                $tempVal = VARS::post($completeKeyName);

                //理由は解らないが、サーバーにより$tempValが配列になる場合とならない場合があるため
                $tempVal = is_array($tempVal) ? $tempVal[0] : $tempVal;

                $tempArray[$keyName] = $tempVal;
            }

            $this->updField[$i] = $tempArray;
        }

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf344Query::getSchoolMst();

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

    //更新ボタン押下時の処理
    public function &getUpdateModel()
    {
        if ($this->rowCount <= 0) {
            $this->warning = "該当データは、ありません。";
            return false;
        }

        knjf344Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return true;
    }

    //確定処理＞確定ボタン押下時の処理
    public function &getFixedUpdateModel()
    {
        knjf344Query::getFixedUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //県への報告ボタン押下時の処理
    public function &getUpdateEdboardModel()
    {
        knjf344Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //CSV出力ボタン押下時
    public function getDownloadModel()
    {
        $CSV_EL = "\n";

        //CSVデータとなる文字列変数
        $contents  = "";

        //ヘッダー行追加
        $header = array(
            '学校名'  ,
            '学年'    ,
            '生徒氏名',
            '検査区分',
            '結果'    ,
        );
        $contents .= implode(',', $header) . $CSV_EL;

        //学校名
        $schoolName = knjf344Query::getEdboardSchoolName();

        //データ行追加
        $dataExist = false;
        $db = Query::dbCheckOut();
        $query = knjf344Query::getCsvData($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学校名をセット
            if ($this->fixedData) {
            } else {
                $row['SCHOOLNAME'] = $schoolName;
            }

            //出力しないデータを削除
            unset($row['EDBOARD_SCHOOLCD']);
            unset($row['SCHREGNO']);

            $row = str_replace(',', '.', $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ',') . $CSV_EL;

            $dataExist = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataExist) {
            $filename = sprintf('%s.csv', $this->title);

            common::downloadContents($contents, $filename, "SJIS-win");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
