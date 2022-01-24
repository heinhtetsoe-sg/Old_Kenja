<?php

require_once('for_php7.php');

class knjf343aModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;//コントロールマスタ
    public $staffcd;//職員コード

    public $selectleft;

    public function knjf343aModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->staffcd = STAFFCD;  //職員コード
        $this->title   = '心電図検査結果集計報告';

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
            '学校名'          ,
            '一次対象者数'    ,
            '一次受検者数'    ,
            '一次受検率％'    ,
            '有所見者数'      ,
            '要精検'          ,
            '主治医管理'      ,
            '放置可'          ,
            'その他'          ,
            '有所見者率％'    ,
            '精密検査該当者'  ,
            '精密検査受検者数',
            '精密検査受検率％',
            '異常なし'        ,
            '要医療'          ,
            '要観察'          ,
            '放置可'          ,
            'その他'          ,
        );
        $contents .= implode(',', $headerTitle) . $CSV_EL;

        //DBからデータ取得
        $db2 = Query::dbCheckOut2();
        $data = $this->getDataWithSql($db2, knjf343aQuery::getCsvData($this));
        Query::dbCheckIn($db2);

        //取得データ行ごとの処理
        $csvColumnKeys = array(//CSV出力する列名
            'SCHOOLNAME'         ,
            'TARGET1'            ,
            'EXAMINEE1'          ,
            'PERCENT1'           ,
            'HAVE_REMARK1'       ,
            'REMARK1_CNT1'       ,
            'REMARK1_CNT2'       ,
            'REMARK1_CNT3'       ,
            'REMARK1_CNT4'       ,
            'HAVE_REMARK_PERCENT',
            'TARGET2'            ,
            'EXAMINEE2'          ,
            'PERCENT2'           ,
            'REMARK2_CNT1'       ,
            'REMARK2_CNT2'       ,
            'REMARK2_CNT3'       ,
            'REMARK2_CNT4'       ,
            'REMARK2_CNT5'       ,
        );
        $rowCount = 0;
        $sum = array();
        foreach ($data as $datum) {
            //データ行ごとにCSV行を追加
            $lineData = array();
            foreach ($csvColumnKeys as $keyName) {
                switch ($keyName) {
                    case 'PERCENT1':
                    case 'HAVE_REMARK_PERCENT':
                    case 'PERCENT2':
                        $lineData[] = $datum[$keyName] . '%';//パーセント表記にする
                        break;
                    default:
                        $lineData[] = $datum[$keyName];
                        break;
                }
            }
            $contents .= implode(',', $lineData) . $CSV_EL;

            //合計値を計算(1行データ毎に加算)
            foreach ($csvColumnKeys as $keyName) {
                switch ($keyName) {
                    case 'SCHOOLNAME':
                    case 'PERCENT1':
                    case 'HAVE_REMARK_PERCENT':
                    case 'PERCENT2':
                        break;//学校名、平均の列では何もしない
                    default:
                        if (is_null($sum[$keyName]) === true) {
                            $sum[$keyName] = 0;
                        }
                        $sum[$keyName] += $datum[$keyName];
                        break;
                }
            }

            //データ行数を増やす
            $rowCount++;
        }

        //平均値を計算(小数第一位まで表示)
        $ave = array();
        $ave['PERCENT1']            = sprintf("%.1f%%", ($sum['TARGET1']   > 0)? (round($sum['EXAMINEE1']    * 100 / (double)$sum['TARGET1'], 1)): 0);
        $ave['HAVE_REMARK_PERCENT'] = sprintf("%.1f%%", ($sum['EXAMINEE1'] > 0)? (round($sum['HAVE_REMARK1'] * 100 / (double)$sum['EXAMINEE1'], 1)): 0);
        $ave['PERCENT2']            = sprintf("%.1f%%", ($sum['TARGET2']   > 0)? (round($sum['EXAMINEE2']    * 100 / (double)$sum['TARGET2'], 1)): 0);

        //合計行を追加
        $sumData = array();
        foreach ($csvColumnKeys as $keyName) {
            //学校名の列は固定の文言
            if ($keyName == 'SCHOOLNAME') {
                $sumData[] = '合計';
                continue;
            }

            //合計値を取得していれば出力、そうでなければブランク
            if (array_key_exists($keyName, $sum) === true) {
                $sumData[] = $sum[$keyName];
            } else {
                $sumData[] = '';
            }
        }
        $contents .= implode(',', $sumData) . $CSV_EL;

        //平均行を追加
        $aveData = array();
        foreach ($csvColumnKeys as $keyName) {
            //学校名の列は固定の文言
            if ($keyName == 'SCHOOLNAME') {
                $aveData[] = '平均';
                continue;
            }

            //平均値を取得していれば出力、そうでなければブランク
            if (array_key_exists($keyName, $ave) === true) {
                $aveData[] = $ave[$keyName];
            } else {
                $aveData[] = '';//ブランク
            }
        }
        $contents .= implode(',', $aveData) . $CSV_EL;

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
