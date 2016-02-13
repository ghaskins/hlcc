;; Licensed to the Apache Software Foundation (ASF) under one
;; or more contributor license agreements.  See the NOTICE file
;; distributed with this work for additional information
;; regarding copyright ownership.  The ASF licenses this file
;; to you under the Apache License, Version 2.0 (the
;; "License"); you may not use this file except in compliance
;; with the License.  You may obtain a copy of the License at
;;
;;   http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing,
;; software distributed under the License is distributed on an
;; "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
;; KIND, either express or implied.  See the License for the
;; specific language governing permissions and limitations
;; under the License.
(ns obcc.dar.ls
  (:require [flatland.protobuf.core :as fl]
            [clojure.java.io :as io]
            [doric.core :as doric]
            [pandect.algo.sha1 :refer :all]
            [pandect.algo.sha256 :refer :all]
            [obcc.dar.types :refer :all]))

(defn read-protobuf [t is] (->> is (fl/protobuf-seq t) first))

(defn verify-compatibility [header] (let [compat (select-keys header [:magic :version])] (= compat CompatVersion)))

(defn read-header [is]
  (if-let [header (read-protobuf Header is)]
    (if (verify-compatibility header)
      header)))

(defn ls [file]
  (with-open [is (io/input-stream file)]
    (if-let [header (read-header is)]
      (let [archive (read-protobuf Archive is)
            payload (->> archive :payload .newInput (fl/protobuf-load-stream Payload))]

        (println (doric/table [{:name :size} {:name :sha1 :title "SHA1"} {:name :path}] (:entries payload)))
        (println "Digital Signature:  none")
        (println "Final Size:        " (.length file) "bytes")
        (println "Chaincode SHA-256: " (sha256 file))))))