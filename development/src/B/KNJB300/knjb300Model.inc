<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb300Model extends Model {

    var $cmd;
    var $year;
    var $semester;
    var $field = array();

    /**
     * メンバ変数の初期化
     */
    function knjb300Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->field);
            $this->auth = AUTHORITY;
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        if (VARS::get("YEAR")) {
            $this->year = VARS::get("YEAR");
        }
        $this->semester = CTRL_SEMESTER;
        if (VARS::get("SEMESTER")) {
            $this->semester = VARS::get("SEMESTER");
        }

        $this->field = array(
                        "CTRL_YEAR"     =>  VARS::post("CTRL_YEAR")         //年度
                        , "CTRL_DATE"     =>  VARS::post("CTRL_DATE")       //日付
                        , "CTRL_SEMESTER" =>  VARS::post("CTRL_SEMESTER")   //学期
                        , "SCHOOL_KIND"   =>  VARS::post("SCHOOL_KIND")     //校種
                        , "TYEP_DATE"     =>  VARS::post("TYEP_DATE")       //対象期間タイプ
                        , "EXECUTEDATE"   =>  VARS::post("EXECUTEDATE")     //対象日
                        , "START_DATE"    =>  VARS::post("START_DATE")      //開始日付
                        , "END_DATE"      =>  VARS::post("END_DATE")        //終了日付
                    );

        // 出力対象クラス
        $this->selectHrClass = array();
        if (VARS::post("SELECT_HR_CLASS")) {
            $this->selectHrClass = explode(',', VARS::post("SELECT_HR_CLASS"));
        }
        // ヘッダ出力
        unset($this->chk_header);
        if (VARS::post("chk_header")) {
            $this->chk_header = VARS::post("chk_header");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * CSV出力
     */
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //テスト//////////////////////////////////////////////////////////
        //ヘッダの作成//
        $header = array();
        $header['EXECUTEDATE'] = "対象日";
        $header['GRADE']       = "学年";
        $header['HR_CLASS']    = "クラス";
        $header['ATTENDNO']    = "出席番号";
        $header['SCHREGNO']    = "学籍番号";
        $header['NAME']        = "生徒氏名";

        // 校時
        $periodList = array();
        $periodCnt = 1;
        $query = knjb300Query::getPeriod($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // 校時CDが 「1」以下の場合は出力しない
            if ($row['ROWNUM'] <= 1) {
                continue;
            }
            $periodList[$row['PERIODCD']] = $row;

            $header['PERIODCD_'.$periodCnt]     = $row['PERIODNAME'];
            $header['CHAIRNAME_'.$periodCnt]    = "講座名";
            $header['SUBCLASSNAME_'.$periodCnt] = "講座科目名称";
            $header['FACILITYNAME_'.$periodCnt] = "講座施設名称";
            $periodCnt++;
        }

        $contents = "";
        if ($this->chk_header) {
            $contents .= implode($header, ",")."\n";
        }

        // データの取得と成形(校時データを１行に取得)
        $contentsData = array();
        $query = knjb300Query::selectCsvQuery($this);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $executeSchData = array();
            if ($contentsData[$row['EXECUTEDATE']][$row['SCHREGNO']]) {
                $executeSchData = $contentsData[$row['EXECUTEDATE']][$row['SCHREGNO']];
            } else {
                $executeSchData['EXECUTEDATE'] = $row['EXECUTEDATE'];
                $executeSchData['GRADE_NAME1'] = $row['GRADE_NAME1'];
                $executeSchData['HR_NAME']     = $row['HR_NAME'];
                $executeSchData['ATTENDNO']    = $row['ATTENDNO'];
                $executeSchData['SCHREGNO']    = $row['SCHREGNO'];
                $executeSchData['NAME']        = $row['NAME'];
                // 校時情報追加
                foreach ($periodList as $key => $period) {
                    $periodData = array();
                    $periodData['PERIODNAME'] = $period['ROWNUM'] - 1;
                    $periodData['CHAIRNAME'] = '';
                    $periodData['SUBCLASSNAME'] = '';
                    $periodData['FACILITYNAME'] = '';

                    $executeSchData['PERIOD'][$key] = $periodData;
                }
                $contentsData[$row['EXECUTEDATE']][$row['SCHREGNO']] = $executeSchData;
            }
            // 校時の情報設定
            $periodData = $executeSchData['PERIOD'][$row['PERIODCD']];
            $periodData['CHAIRNAME'] .= $periodData['CHAIRNAME'] ? "、".$row['CHAIRNAME'] : $row['CHAIRNAME'];
            $periodData['SUBCLASSNAME'] .= $periodData['SUBCLASSNAME'] ? "、".$row['SUBCLASSNAME'] : $row['SUBCLASSNAME'];
            // 施設情報
            $facility = array();
            $query2 = knjb300Query::getFacility($this, $row['EXECUTEDATE'], $row['PERIODCD'], $row['CHAIRCD']);
            $result2 = $db->query($query2);
            while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
                $periodData['FACILITYNAME'] .= $periodData['FACILITYNAME'] ? "、".$row2['FACILITYNAME'] : $row2['FACILITYNAME'];
            }
            $executeSchData['PERIOD'][$row['PERIODCD']] = $periodData;
            $contentsData[$row['EXECUTEDATE']][$row['SCHREGNO']] = $executeSchData;
        }
        $result->free();
        Query::dbCheckIn($db);

        // データ出力有無
        $isData = false;
        // データの出力
        foreach ($contentsData as $executeDate => $executeData) {
            foreach ($executeData as $schregNo => $value) {
                $isData = true;

                $data = array();
                $data[] = $value['EXECUTEDATE'];
                $data[] = $value['GRADE_NAME1'];
                $data[] = $value['HR_NAME'];
                $data[] = $value['ATTENDNO'];
                $data[] = $value['SCHREGNO'];
                $data[] = $value['NAME'];

                foreach ($value['PERIOD'] as $key => $period) {
                    $data[] = $period['PERIODNAME'];
                    $data[] = $period['CHAIRNAME'];
                    $data[] = $period['SUBCLASSNAME'];
                    $data[] = $period['FACILITYNAME'];
                }

                $data[] = implode($facility, "、");
                $contents .= implode($data, ",")."\n";
            }
        }

        if ($isData){
            $couse="時間割データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
