<?php
class knjh140cquery extends Query
{

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            //登録用データを作成
            //$insData = array();
            $insData = knjh140cQuery::setInsertData($data_arr, $i);

            knjh140cQuery::deleteQuery($model, $db, $data_arr, $i); //削除SQLの呼び出し
            if ($insData["ROSEN_1"] != "徒歩" && $insData["ROSEN_1"] != "") {
                knjh140cQuery::insertQuery($model, $db, $insData);
            } //登録SQLの呼び出し
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            knjh140cQuery::deleteQuery($model, $db, $data_arr, $i);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //データ削除
    public function deleteQuery($model, $db, $data_arr, $i)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO   = '".$data_arr[$i]["SCHREGNO"]."' ";
        $db->query($query);
    }

    //データ登録
    public function insertQuery($model, $db, $insData)
    {

        //項目
        $data = array();
        $data["SCHREGNO"][TEXT]         = $insData["SCHREGNO"];
        
        for ($i = 1; $i <= 7; $i++) {
            $josya = $insData["JOSYA_".$i];
            $rosen = $insData["ROSEN_".$i];
            $gesya = $insData["GESYA_".$i];
            $flg   = $insData["FLG_".$i];

            $data["JOSYA_".$i][TEXT]           = $josya;        //乗車駅
            $data["ROSEN_".$i][TEXT]           = $rosen;        //路線
            $data["GESYA_".$i][TEXT]           = $gesya;        //下車駅
            $data["FLG_".$i][TEXT]             = $flg;          //通学手段
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";

        //追加
        $query = Query::insertSQL($data, "SCHREG_ENVIR_DAT");
        $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
    
    //登録用データの作成
    public function setInsertData(&$data_arr, $i)
    {
        $data = array("SCHREGNO"    => $data_arr[$i]["SCHREGNO"],
                     );

        $keiyuFlg = 0; //経由フラグ 0：経由なし 1:経由あり(乗車⇒経由) 2:経由あり(経由⇒下車)
        $idx = 7;
        for ($j = 1; $j <= 7; $j++) {
            while ($data_arr[$i]["ROSEN_".$idx] == "" && $idx > 0) {
                $idx = $idx - 1;
            }

            //学校から自宅までの順路で来るため、自宅から学校の順路に変更して格納する
            //経由がnullの場合、乗車⇒下車の1区間で扱う
            //経由がnullでない場合、乗車⇒経由、経由⇒下車の2区間で扱う
            //※順路が逆となる為、乗車はCSVの下車、下車はCSVの乗車で考える
            if ($idx > 0) {
                if ($data_arr[$i]["KEIYU_".$idx] != "") {
                    if ($keiyuFlg == 1) {
                        //経由⇒下車
                        $josya = $data_arr[$i]["KEIYU_".$idx];
                        $gesya = $data_arr[$i]["JOSYA_".$idx];
                        $keiyuFlg = 2;
                    } else {
                        //乗車⇒経由
                        $josya = $data_arr[$i]["GESYA_".$idx];
                        $gesya = $data_arr[$i]["KEIYU_".$idx];
                        $keiyuFlg = 1;
                    }
                } else {
                    //乗車⇒下車
                    $josya = $data_arr[$i]["GESYA_".$idx];
                    $gesya = $data_arr[$i]["JOSYA_".$idx];
                    $keiyuFlg = 0;
                }
                $rosen = $data_arr[$i]["ROSEN_".$idx];
                $flg   = "2"; //通学手段 = 2:その他
            } else {
                $josya = "";
                $rosen = "";
                $gesya = "";
                $flg   = "";
            }

            $data = array_merge($data, array("JOSYA_".$j  => $josya)); // 乗車駅
            $data = array_merge($data, array("ROSEN_".$j  => $rosen)); // 路線
            $data = array_merge($data, array("GESYA_".$j  => $gesya)); // 下車駅
            $data = array_merge($data, array("FLG_".$j    => $flg));   // 通学手段

            if ($keiyuFlg != 1) {
                $idx = $idx - 1;
            }
        }
        
    
        return $data;
    }
}
